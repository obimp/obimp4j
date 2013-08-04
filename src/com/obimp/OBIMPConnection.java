/*
 * OBIMP4J - Java OBIMP Lib
 * Copyright (C) 2013 alex_xpert
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.obimp;

import com.obimp.data.DataType;
import com.obimp.data.structure.wTLD;
import com.obimp.data.type.LongWord;
import com.obimp.data.type.QuadWord;
import com.obimp.data.type.UTF8;
import com.obimp.data.type.Word;
import com.obimp.listener.ConnectionListener;
import com.obimp.listener.MessageListener;
import com.obimp.listener.UserStatusListener;
import com.obimp.packet.Packet;
import com.obimp.packet.PacketHandler;
import com.obimp.packet.PacketListener;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.net.Socket;
import java.util.Vector;

/**
 * Подключение к OBIMP (Bimoid) серверу
 * @author alex_xpert
 */
public class OBIMPConnection {
    private String server = "";
    private String username = "";
    private String password = "";
    
    private Socket con;
    private DataInputStream in;
    protected DataOutputStream out;
    
    private PacketListener listener;
    
    public int seq = 0;
    
    public Vector<ConnectionListener> con_list = new Vector<ConnectionListener>();
    public Vector<MessageListener> msg_list = new Vector<MessageListener>();
    public Vector<UserStatusListener> stat_list = new Vector<UserStatusListener>();
    
    public static boolean connected = false;
    
    public OBIMPConnection(String server, String username, String password) {
        this.server = server;
        this.username = username;
        this.password = password;
    }
    
    public synchronized int getSeq() {
        return seq++;
    }
    
    public void addConnectionListener(ConnectionListener cl) {
        con_list.add(cl);
    }
    
    public void addMessageListener(MessageListener ml) {
        msg_list.add(ml);
    }
    
    public void addUserStatusListener(UserStatusListener usl) {
        stat_list.add(usl);
    }
    
    public void connect() {
        try {
            connected = true;
            con = new Socket(server, 7023);
            in = new DataInputStream(con.getInputStream());
            out = new DataOutputStream(con.getOutputStream());
            listener = new PacketListener(con, in, this, username, password);
            
            try {
                Thread t = new Thread(listener);
                //t.setDaemon(true);
                t.start();
                
                Packet hello = new Packet(0x0001, 0x0001); // OBIMP_BEX_COM_CLI_HELLO
                hello.append(new wTLD(0x00000001, new UTF8(username)));
                out.write(hello.asByteArray(getSeq()));
                out.flush();
                while(!PacketHandler.logged) {
                    Thread.sleep(100);
                }
//                Packet login = new Packet(0x0001, 0x0003); // OBIMP_BEX_COM_CLI_LOGIN
//                login.append(new wTLD(0x00000001, new UTF8(username)));
//                login.append(new wTLD(0x00000002, new OctaWord(hash)));
//                out.write(login.asByteArray(getSeq()));
//                out.flush();
                Packet cl_params = new Packet(0x0002, 0x0001); // OBIMP_BEX_CL_CLI_PARAMS
                out.write(cl_params.asByteArray(getSeq()));
                out.flush();
                Packet pres_params = new Packet(0x0003, 0x0001); // OBIMP_BEX_PRES_CLI_PARAMS
                out.write(pres_params.asByteArray(getSeq()));
                out.flush();
                Packet im_params = new Packet(0x0004, 0x0001); // OBIMP_BEX_IM_CLI_PARAMS
                out.write(im_params.asByteArray(getSeq()));
                out.flush();
                Packet ud_params = new Packet(0x0005, 0x0001); // OBIMP_BEX_UD_CLI_PARAMS
                out.write(ud_params.asByteArray(getSeq()));
                out.flush();
                Packet ua_params = new Packet(0x0006, 0x0001); // OBIMP_BEX_UA_CLI_PARAMS
                out.write(ua_params.asByteArray(getSeq()));
                out.flush();
                Packet ft_params = new Packet(0x0007, 0x0001); // OBIMP_BEX_FT_CLI_PARAMS
                out.write(ft_params.asByteArray(getSeq()));
                out.flush();
                Packet tp_params = new Packet(0x0008, 0x0001); // OBIMP_BEX_TP_CLI_PARAMS
                out.write(tp_params.asByteArray(getSeq()));
                out.flush();
                Packet req_pres_info = new Packet(0x0003, 0x0008); // OBIMP_BEX_PRES_CLI_REQ_PRES_INFO
                out.write(req_pres_info.asByteArray(getSeq()));
                out.flush();
                Packet cl_req = new Packet(0x0002, 0x0003); // OBIMP_BEX_CL_CLI_REQUEST
                out.write(cl_req.asByteArray(getSeq()));
                out.flush();
                Packet verify = new Packet(0x0002, 0x0005); // OBIMP_BEX_CL_CLI_VERIFY
                out.write(verify.asByteArray(getSeq()));
                out.flush();
                Packet pres_info = new Packet(0x0003, 0x0003); // OBIMP_BEX_PRES_CLI_SET_PRES_INFO
                pres_info.append(new wTLD(0x00000001, new DataType[] {new Word(0x0001), new Word(0x0002)}));
                pres_info.append(new wTLD(0x00000002, new Word(0x0002)));
                pres_info.append(new wTLD(0x00000003, new UTF8("Java OBIMP Lib (OBIMP4J)")));
                pres_info.append(new wTLD(0x00000004, new QuadWord(0, 1, 0, 0, 0, 3, 0, 7)));
                pres_info.append(new wTLD(0x00000005, new Word(0x0052)));
                pres_info.append(new wTLD(0x00000006, new UTF8(System.getProperty("os.name") + " " +
                        (System.getProperty("os.arch").contains("64") ? "x64" : "x86")))); //операционная система
                out.write(pres_info.asByteArray(getSeq()));
                out.flush();
                Packet set_status = new Packet(0x0003, 0x0004); // OBIMP_BEX_PRES_CLI_SET_STATUS
                set_status.append(new wTLD(0x00000001, new LongWord(0, 0, 0, Status.PRES_STATUS_ONLINE)));
                //set_status.append(new wTLD(0x00000002, new UTF8("Работает!")));
                //set_status.append(new wTLD(0x00000004, new UTF8("Моя библиотека работает!")));
                //set_status.append(new wTLD(0x00000005, new UUID(1, XStatus.COFFEE)));
                out.write(set_status.asByteArray(getSeq()));
                out.flush();
                Packet activate = new Packet(0x0003, 0x0005); // OBIMP_BEX_PRES_CLI_ACTIVATE
                out.write(activate.asByteArray(getSeq()));
                out.flush();
                Packet ud_details = new Packet(0x0005, 0x0003); // OBIMP_BEX_UD_CLI_DETAILS_REQ
                ud_details.append(new wTLD(0x0001, new UTF8(username)));
                out.write(ud_details.asByteArray(getSeq()));
                out.flush();
                Packet req_offline_msgs = new Packet(0x0004, 0x0003); // OBIMP_BEX_IM_CLI_REQ_OFFLINE
                out.write(req_offline_msgs.asByteArray(getSeq()));
                out.flush();
                Packet del_offline_msgs = new Packet(0x0004, 0x0005); // OBIMP_BEX_IM_CLI_DEL_OFFLINE
                out.write(del_offline_msgs.asByteArray(getSeq()));
                out.flush();
            } catch(Exception ex) {
                System.out.println("Error:\n");
                ex.printStackTrace();
            }
        } catch(Exception ex) {
            System.out.println("Error:\n");
            ex.printStackTrace();
        }
    }
    
    public void sendPong() {
        try {
            Packet pong = new Packet(0x0001, 0x0007); // OBIMP_BEX_COM_CLI_SRV_KEEPALIVE_PONG
            out.write(pong.asByteArray(getSeq()));
        } catch(Exception ex){
            System.out.println("Error:" + ex);
        }
    }
    
    public void disconnect() {
        try {
            if(con.isConnected()) con.close();
            seq = 0;
            connected = false;
            for(ConnectionListener cl : con_list) {
                cl.onLogout("USER_DISCONNECTED");
            }
        } catch(Exception ex){
            System.out.println("Error:" + ex);
        }
    }

}
