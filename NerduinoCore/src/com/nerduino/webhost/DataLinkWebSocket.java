package com.nerduino.webhost;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nerduino.library.NerduinoBase;
import com.nerduino.library.NerduinoManager;
import com.nerduino.library.PointManager;
import com.nerduino.skits.Skit;
import com.nerduino.skits.SkitManager;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

public class DataLinkWebSocket extends WebSocketAdapter
{
	private ObjectMapper mapper = new ObjectMapper();
	boolean monitoring = false;
	long lastping = System.currentTimeMillis();

	DataLink[] links;
	int linkcount = 0;

	public boolean getMonitoring()
	{
		return monitoring;
	}
	
	public void setMonitoring(boolean value)
	{
		monitoring = value;
	}
	
	@Override
	public void onWebSocketClose(int statusCode, String message)
	{
		super.onWebSocketClose(statusCode, message);
		
		closeMonitoringSession();
	}

	@Override
	public void onWebSocketConnect(Session session)
	{
		super.onWebSocketConnect(session);
	}

	
	@Override
	public void onWebSocketText(String message)
	{
		try	
		{
			String querrystr = message;

			if (querrystr.startsWith("\"") && querrystr.endsWith("\""))
				querrystr = "{" + querrystr.substring(1, querrystr.length() - 1) + "}";
			else if (!querrystr.startsWith("{"))
				querrystr = "{" + querrystr + "}";

			JsonNode rootNode = mapper.readTree(querrystr);

			JsonNode commandnode = rootNode.findValue("command");
			RemoteEndpoint endpoint = this.getRemote();

			if (commandnode != null)
			{
				String command = commandnode.textValue();

				if (command.equalsIgnoreCase("exit"))
				{
					closeMonitoringSession();
				}
				else if (command.equalsIgnoreCase("monitor"))
				{
					JsonNode linksnode = rootNode.findValue("data");

					if (linksnode != null)
					{
						linkcount = linksnode.size();	

						links = new DataLink[linkcount];

						for(int i = 0; i < linkcount; i++)
						{
							JsonNode linknode = linksnode.get(i);

							DataLink link = new DataLink();

							link.id = i;
							link.path = linknode.get("path").textValue();
							link.filterValue = 0.0f;
							link.filterType = 0;
							link.lastValue = 0.0f;
							link.status = 1; // unresolved

							JsonNode filternode = linknode.get("filter");

							if (filternode != null)
							{
								link.filterType = filternode.asInt();

								JsonNode valuenode = linknode.get("value");

								if (valuenode != null)
									link.filterValue = (float) valuenode.asDouble();
							}

							links[i] = link;
						}

						// loop through links until the connection is closed
						new Thread(new Runnable() 
						{
							public void run() 
							{
								try 
								{
									RemoteEndpoint endpoint = getRemote();
									setMonitoring(true);
									
									while(getMonitoring())
									{
										for(int i = 0; i < linkcount; i++)
										{
											DataLink link = links[i];

											link.currentValue++;

											switch(link.status)
											{
												case 1: // un resolved, attempt to bind to the specified path
												{
													String path = link.path;

													// check for '.' in the path to indicate that the path is of the form nerduino.point or a local point
													int p = path.indexOf(".");

													if (p > 0)
													{
														String nerduinoname = path.substring(0, p);
														String pointname = path.substring(p + 1);

														// register for point value updates
														NerduinoBase nerduino = NerduinoManager.Current.getNerduino(nerduinoname);

														if (nerduino != null)
														{
															link.remoteDataPoint = nerduino.getPoint(pointname);

															if (link.remoteDataPoint != null)
															{
																switch(link.filterType)
																{
																	case 0: //FT_NoFilter
																		link.remoteDataPoint.registerWithNoFilter((short) i);
																		break;
																	case 1: //FT_PercentChange(1),
																		link.remoteDataPoint.registerWithPercentFilter((short) i, link.filterValue);
																		break;
																	case 2: //FT_ValueChange(2);
																		link.remoteDataPoint.registerWithChangeFilter((short) i, link.filterValue);
																		break;
																}

																link.status = 2;
															}
															else
															{
																link.status = 3; // cannot resolve
															}
														}
														else
														{
															// attempt to resolve again, but throttle
															Thread.sleep(25);
														}
													}
													else
													{
														// get the local data point with this name
														link.localDataPoint = PointManager.Current.getPoint(path);

														if (link.localDataPoint != null)
															link.status = 2;
														else
															// attempt to resolve again, but throttle
															Thread.sleep(25);
													}
												}
													break;
												case 2: // resolved
												{
													boolean updated = false;

													try
													{
														if (link.localDataPoint != null)
															link.currentValue = link.localDataPoint.getFloat();
														else if (link.remoteDataPoint != null)
															link.currentValue = link.remoteDataPoint.getFloat();

														switch(link.filterType)
														{
															case 0: // FT_NoFilter:
																if (link.lastValue != link.currentValue)
																	updated = true;

																break;
															case 1: // FT_ValueChange:
															{
																double dif = Math.abs(link.lastValue - link.currentValue);

																if (dif >= link.filterValue)
																	updated = true;
															}
																break;
															case 2: // FT_PercentChange:
															{
																if (link.lastValue == 0.0)
																{
																	if (link.currentValue != 0.0)
																		updated = true;
																}
																else
																{
																	double dif = link.lastValue - link.currentValue;
																	double percent = Math.abs(dif / link.lastValue * 100.0);

																	if (percent >= link.filterValue)
																		updated = true;
																}
															}
																break;
														}
													}
													catch(Exception e)
													{
														setMonitoring(false);
													}

													if (updated)
													{
														link.lastValue = link.currentValue;

														String str = "{\"id\" : " + Integer.toString(i) + ", \"val\" : " + Double.toString(link.currentValue) + "}";

														endpoint.sendString(str);													
													}
												}
												break;
											}
										}

										try
										{
											// throttle the loop
											Thread.sleep(1);
										}
										catch(Exception e)
										{
											setMonitoring(false);
										}

										try
										{
											// throttle responses
											Thread.sleep(50);
										}
										catch(Exception e)
										{
											setMonitoring(false);
										}
									}
								} 
								catch (Exception e) 
								{
								}	
							}
						}, "DataLink Monitor").start();
					}
				}
				else if (command.equalsIgnoreCase("getskits"))
				{
					try
					{
						Node[] nodes = SkitManager.Current.getChildren().getNodes();

						String[] skits = new String[nodes.length];
						int i = 0;

						for(Node node : nodes)
						{
							Skit skit = (Skit) node;
							String name = skit.getName();

							skits[i++] = name;
						}

						String str = mapper.writeValueAsString(skits);
						
						endpoint.sendString(str);
					}
					catch(JsonGenerationException ex)
					{
						Exceptions.printStackTrace(ex);
					}
					catch(JsonMappingException ex)
					{
						Exceptions.printStackTrace(ex);
					}
				}
				else if (command.equalsIgnoreCase("get"))
				{
					JsonNode data = rootNode.findValue("data");
					
					if (data != null)
					{
						String path = data.textValue();
						
						// resolve the path
						Object value = NerduinoManager.Current.getPointValue(path);

						if (value != null)
							endpoint.sendString(value.toString());
						else
							// if not found, report an unrecognized path
							endpoint.sendString("Unrecognized path!");
					}
				}
				else if (command.equalsIgnoreCase("set"))
				{
					JsonNode data = rootNode.findValue("data");
					
					if (data != null)
					{
						int index = data.get("id").asInt();
						
						DataLink link = links[index];

						if (link.status == 2)
						{
							String value = data.get("value").asText();

							link.lastValue = Float.parseFloat(value);

							if (link.localDataPoint != null)
								link.localDataPoint.setValue(value);
							else if (link.remoteDataPoint != null)
								link.remoteDataPoint.setValue(value);
						}
					}
					//else
					//{
					//	endpoint.sendString("Path/Value not provided!");
					//}
				}
				else
				{
					endpoint.sendString("Unrecognized request");
				}
			}
		}
		catch(Exception e)
		{
			closeMonitoringSession();
		}
	}
	
	void closeMonitoringSession()
	{
		if (monitoring)
		{
			monitoring = false;
			
			// unregister points
			// close socket
			for(DataLink link : links)
			{
				if (link.remoteDataPoint != null)
				{
					link.remoteDataPoint.unregister();
				}
			}
			
			linkcount = 0;
			links = null;
		}
	}
}
