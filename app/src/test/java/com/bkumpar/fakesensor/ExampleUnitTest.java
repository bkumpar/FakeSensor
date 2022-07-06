package com.bkumpar.fakesensor;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.bkumpar.odooconnectorlib.OdooConnector;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    private static final String TAG = "odooConnector";
    private final String url = "http://192.168.111.35:8069";
    private final String database = "meca_demo_tvin";
    private final String username = "admin";
    private final String password = "admin";
    private final Integer userID = 2;
    private  final String productionName = "WH/MO/00062";
    private OdooConnector odooConnector;

    @Before
    public void setUp() throws Exception {
        odooConnector = new OdooConnector(url,database);
//        Assert.assertTrue(odooConnector.authenticate(username, password));
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void connectionOK() throws Exception {
        try {
            ArrayList<Object> ret =  odooConnector.getServerInfo() ;
            int length = ret.size();
            Assert.assertEquals(1,ret.size());
            HashMap<String,Object> info = (HashMap<String,Object>) ret.get(0);

            int protocolVersion = (int)info.get("protocol_version");
            Assert.assertEquals(1, protocolVersion);

            String serverVersion = (String)info.get("server_version");
            Assert.assertEquals("14.0", serverVersion);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void databaseExists() throws Exception {
        try {
            ArrayList<Object> ret = odooConnector.getDatabases() ;
            int length = ret.size();
            ArrayList<String> databases = (ArrayList<String>)ret.get(0);
            Assert.assertTrue(databases.contains(database));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void authentificationOK() throws Exception {
        Assert.assertTrue(odooConnector.authenticate(username, password));
    }


}