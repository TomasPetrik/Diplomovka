package com.pop24.androidapp.external_dbs;

import android.util.Log;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class JDBCMySQLConnection {
	private static JDBCMySQLConnection instance = new JDBCMySQLConnection();
	private String tag = "JDBCMySQLConnection";
	
	private JDBCMySQLConnection() {
        try {
            //Step 2: Load MySQL Java driver
            Class.forName(Config.DRIVER_CLASS);
           
            
            
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("ERROR: Driver Class, ClassNotFoundException, :" + e.toString());
        }
    }
	
	private void doSshTunnel( String strSshUser, String strSshPassword, String strSshHost, int nSshPort, String strRemoteHost, int nLocalPort, int nRemotePort ) throws JSchException
	  {
	    final JSch jsch = new JSch();
	    Session session = jsch.getSession( strSshUser, strSshHost, 22 );
	    session.setPassword( strSshPassword );
	     
	    final Properties config = new Properties();
	    config.put( "StrictHostKeyChecking", "no" );
	    session.setConfig( config );
	    //jsch.addIdentity("ssh/id_rsa.jpg");
	    session.connect();
	    session.setPortForwardingL(nLocalPort, strRemoteHost, nRemotePort);
	    
	    Log.d(tag, "Succes do SshTunel" + session.getServerVersion() + " " + session.getPortForwardingL());
	  }
	
	private Connection createConnection() {
		 
        Connection connection = null;
        try {
            Properties connInfo = new Properties();
            connInfo.put("user", Config.USER);
            connInfo.put("password",  Config.PASSWORD);
            connInfo.put("charSet", "utf8");

            connection = DriverManager.getConnection(Config.URL, connInfo);


        } catch (SQLException e) {
            System.out.println("ERROR: Unable to Connect to Database, :" + e.toString());
        }
          
        return connection;
    } 
	
	private Connection createSecureConnection() {
		 
        Connection connection = null;
        try {
        	
        	
        	String strSshUser = Config.SSH_USER; 		//"ssh_user_name";                  // SSH loging username
            String strSshPassword = Config.SSH_PASSWORD;	//"abcd1234";                   // SSH login password
            String strSshHost = Config.PUBLIC_IP; 		//"your.ssh.hostname.com";          // hostname or ip or SSH server
            int nSshPort = 22;                                    // remote SSH host port number
            String strRemoteHost = Config.LOCAL_IP;  // hostname or ip of your database server
            int nLocalPort = 3366;                                // local port number use to bind SSH tunnel
            int nRemotePort = 3306;                               // remote port number of your database 
            String strDbUser = Config.USER;                    // database loging username
            String strDbPassword = Config.PASSWORD;                    // database login password
             
            doSshTunnel(strSshUser, strSshPassword, strSshHost, nSshPort, strRemoteHost, nLocalPort, nRemotePort);
             
            Class.forName("com.mysql.jdbc.Driver");
            //connection = DriverManager.getConnection(Config.URL, Config.USER, Config.PASSWORD);
            
            connection = DriverManager.getConnection("jdbc:mysql://localhost:" + nLocalPort, strDbUser, strDbPassword);
            
            Log.d(tag, "Succes jdbc connection");
            //con.close();


        } catch (SQLException e) {
            Log.e(tag, "ERROR: Unable to Connect to Database, :" + e.toString());
        }catch( Exception e )
        {
           e.printStackTrace();
        }
          
        return connection;
    } 
	
	public static Connection getConnection() {
        return instance.createConnection();
    }
     
	
}
