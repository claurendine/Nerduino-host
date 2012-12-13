package com.nerduino.webhost;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;


public class NerduinoServlet extends HttpServlet 
{
    private int count = 0;
	private static final boolean logenabled = false;


	// / Constructor.
	public NerduinoServlet() 
    {
	}


    @Override
    public void init(ServletConfig config) throws ServletException 
    {
        super.init(config);
        getServletContext().log("init() called");
        count=0;
    }
 
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException 
    {
        // parse the incoming url
        // discard the servlet path
        // parse the request
        // syntax:
        //    getSkits
        //    getSkit/skitname
        //    getNerduinos
        //    getNerduino/nerduino
        //    getPoints/nerduino
        //    getPoint/nerduino/index
        //    getValue/nerduino/index
        //    getValueByName/nerduino/name
        //    setValue/nerduino/index/value
        //    streamValues/name,filtertype,filtervalue/name,filtertype,filtervalue/...
        
        String query = request.getRequestURI();
        
        String[] parts = query.split("/");
        
        if (parts.length > 2)
        { 
            String command = parts[2];
            
            if (command.equalsIgnoreCase("getskits"))
            {
				/*
				ABC abc = new ABC("Aaa", 123);
				
				ObjectMapper mapper = new ObjectMapper();

				try
				{
					PrintWriter writer = response.getWriter();
					
					for(int ii = 0; ii < 3000; ii++)
					{
						abc.aa = "A"+ ii;
						
						String str = mapper.writeValueAsString(abc);
						
						writer.write(str);
						
						writer.flush();
						
						Thread.sleep(20);
						
					}
				}
				
				catch(InterruptedException ex)
				{
					Logger.getLogger(NerduinoServlet.class.getName()).log(Level.SEVERE, null, ex);
				}				catch(IOException ex)
				{
//					Logger.getLogger(HostManagerView.class.getName()).log(Level.SEVERE, null, ex);
				}
				*/
                //response.getWriter().write("GetSkits");
            }
            else if (command.equalsIgnoreCase("getskit"))
            {
                response.getWriter().write("GetSkit");            
            }
            else if (command.equalsIgnoreCase("getnerduinos"))
            {
                response.getWriter().write("GetNerduinos");            
            }
            else if (command.equalsIgnoreCase("getnerduino"))
            {
                response.getWriter().write("GetNerduino");            
            }
            else if (command.equalsIgnoreCase("getpoints"))
            {
                response.getWriter().write("GetPoints");            
            }
            else if (command.equalsIgnoreCase("getpoint"))
            {
                response.getWriter().write("GetPoint");            
            }
            else if (command.equalsIgnoreCase("getvalue"))
            {
                response.getWriter().write("GetValue");            
            }
            else if (command.equalsIgnoreCase("getvaluebyname"))
            {
                response.getWriter().write("GetValues");            
            }
            else if (command.equalsIgnoreCase("setvalue"))
            {
                response.getWriter().write("SetValue");            
            }
            else if (command.equalsIgnoreCase("streamvalues"))
            {
                ServletOutputStream os = response.getOutputStream();

                for(int i = 0; i < 50; i++)
                {
                    //response.getWriter().write("Incrementing the count: Count = " + i);

                    os.println("Incrementing the count: Count = " + i);
                    os.flush();

                    try
                    {
                        Thread.sleep(500);
                    }
                    catch(Exception e)
                    {
                    }
                }            
            }
            else
            {
                response.getWriter().write("Unrecognized request");
            }
        }
                
        getServletContext().log("service() called");
        count++;
        

    }
 
    @Override
    public void destroy() 
    {
        getServletContext().log("destroy() called");
    }     
 
	
    // / Returns a string containing information about the author, version, and
	// copyright of the servlet.
	public String getServletInfo() 
    {
		return "Nerduino Servlet";
	}

	public void log(String msg) {
		if (logenabled)
			super.log(msg);
	}
}