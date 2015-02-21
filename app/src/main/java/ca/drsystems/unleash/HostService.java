package ca.drsystems.unleash;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class HostService extends AsyncTask<Void, Void, String>{
	
	public Handler handler;
    private List<ClientService> clientServiceList;
	public final static int PORT = 12345;
	public ServerSocket server;
	public Play PlayAct;
	private int user_num;
	public WifiP2pDevice device;
	private boolean run;
	public Socket client;
	public OutputStream os;
	public InputStream is;

	//private String message;
	
	public HostService(Handler h, Play a){
		this.handler = h;
		this.PlayAct = a;
		this.run = false;
		Log.v("SOCK", "HostService constructor");
		createSockets();
        User u = new User();
        u.setNumber(0);
        Play.UserLocations.setMyUser(0);
        Play.UserLocations.setUser(u);
        PlayAct.startLocationRequest();

	}
	
	private void createSockets(){
		// create Socket for Server
		try {
			Log.v("SOCK", "Try to create ServerSocket on port: " + PORT);
			server = new ServerSocket(PORT);
			Log.v("SOCK", "ServerSocket creation successful.");
			run = true;
		} catch (IOException e) {
			Log.v("SOCK", "IOException HostService.java: in createSockets() trying to create ServerSocket()");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    public void sendToAll(int header, Object o)
    {
        for(ClientService iter : clientServiceList)
        {
            iter.send(header, o);
        }
    }

	@Override
	protected String doInBackground(Void... params) {
		user_num = 1;
		
		while(!run){
			Log.v("SOCK", "ServerSocket not created yet");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		while(run){
			try {
				Log.v("SOCK", "While run==TRUE, wait for client: " + user_num + "'s socket to connect");
				client = server.accept();
				Log.v("SOCK", "We got a client! client's InetAddress: " + client.getInetAddress());
				os = client.getOutputStream();
				is = client.getInputStream();
				Thread thread = new Thread(new Runnable(){
				    @Override
				    public void run() {
				        try {
				        	Log.v("SOCK", "Creating new Thread for client: " + user_num);
				        	ClientService temp = new ClientService(handler, PlayAct, device, user_num, client, os, is);
                            clientServiceList.add(temp);
                            PlayAct.connected++;
				        	user_num++;
				        } catch (Exception e) {
				            e.printStackTrace();
				        }
				    }
				});
				
				thread.start();
				
			} catch (IOException e) {
                Log.v("SOCK", "IOException creating IO streams for client " + user_num);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		return null;
	}
}
