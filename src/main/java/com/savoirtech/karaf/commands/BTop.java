/*
 * BTop
 *
 * Copyright (c) 2014, Savoir Technologies, Inc., All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */

package com.savoirtech.karaf.commands;

import java.io.IOException;
import java.net.MalformedURLException;

import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;

import org.apache.activemq.broker.jmx.BrokerViewMBean;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.AbstractAction;

@Command(scope = "aetos", name = "btop", description = "ActiveMQ Karaf Top Command")
public class BTop extends AbstractAction {

    private int             DEFAULT_REFRESH_INTERVAL = 1000;
    private MBeanServerConnection conn = null;

    @Option(name = "-u", aliases = { "--updates" }, description = "Update interval in milliseconds", required = false, multiValued = false)
    private String updates;


    protected Object doExecute() throws Exception {
        if (updates != null) {
            DEFAULT_REFRESH_INTERVAL = Integer.parseInt(updates);
        } 
        try {
            Map env = new HashMap();
            env.put(JMXConnector.CREDENTIALS,new String[]{"admin","activemq"});
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi");
            JMXConnector jmxc = JMXConnectorFactory.connect(url, env);
            conn = jmxc.getMBeanServerConnection();
            BTop(conn);
        } catch (MalformedURLException badUrl) {
            System.err.println("Error: Problem with url: " + badUrl.getMessage());
        } catch (IOException ioe) {
            System.err.println("Error: IOException trying to connect to JMX: " + ioe.getMessage());
        } catch (Exception e) {
            //Ignore
            System.out.println(e.getMessage());
        }
        return null;
    }

    private void BTop(MBeanServerConnection conn) throws InterruptedException, IOException, Exception {

        if (conn == null) {
            System.out.println("Could not connect to ActiveMQ Broker.");
        } else {

            // Continously update stats to console.
            while (true) {
            Thread.sleep(DEFAULT_REFRESH_INTERVAL);
            //Clear console
            clearScreen();
            printBrokerStats(conn);
            System.out.println();
            System.out.println("\u001B[36m==========================================================================================\u001B[0m");
            System.out.println();
            //printTopStats();
            // Display notifications
            System.out.printf(" Note: ActiveMQ Broker stats updated at  %d ms intervals", DEFAULT_REFRESH_INTERVAL);
            System.out.println();
            System.out.println("\u001B[36m==========================================================================================\u001B[0m");
            }
        }
    }

    private void clearScreen() {
        System.out.print("\33[2J");
        System.out.flush();
        System.out.print("\33[1;1H");
        System.out.flush();
    }

    private void printBrokerStats(MBeanServerConnection conn) throws Exception {

        ObjectName activeMQ = new ObjectName("org.apache.activemq:type=Broker,brokerName=amq-broker");
        BrokerViewMBean mbean = (BrokerViewMBean) MBeanServerInvocationHandler.newProxyInstance(conn, activeMQ, BrokerViewMBean.class, true);

        System.out.printf(" btop - BrokerID: %S BrokerName: %S BrokerVersion: %S %n", 
                            mbean.getBrokerId(), 
                            mbean.getBrokerName(),
                            mbean.getBrokerVersion());
        System.out.printf(" More stats here...  %n");
    }

    private void printTopStats() {
    }

}
