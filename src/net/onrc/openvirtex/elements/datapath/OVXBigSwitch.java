/**
 * Copyright (c) 2013 Open Networking Laboratory
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so,
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */
package net.onrc.openvirtex.elements.datapath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.onrc.openvirtex.core.io.OVXSendMsg;
import net.onrc.openvirtex.elements.OVXMap;
import net.onrc.openvirtex.elements.link.PhysicalLink;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.elements.port.PhysicalPort;
import net.onrc.openvirtex.messages.Devirtualizable;
import net.onrc.openvirtex.routing.ManualRoute;
import net.onrc.openvirtex.routing.Routable;
import net.onrc.openvirtex.routing.RoutingAlgorithms;
import net.onrc.openvirtex.routing.SwitchRoute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPhysicalPort;

/**
 * The Class OVXBigSwitch.
 * 
 */

public class OVXBigSwitch extends OVXSwitch {

    private static Logger                                                      	log = LogManager
	                                                                                   .getLogger(OVXBigSwitch.class
	                                                                                           .getName());

    /** The alg. */
    private RoutingAlgorithms                                                  	alg;

    /** The path map. */
    private final HashMap<OVXPort, HashMap<OVXPort, LinkedList<PhysicalLink>>> 	pathMap;

    /** The routing mechanism */
    private Routable 								routing;
    
    /**The calculated routes*/
    private final HashMap<OVXPort, HashMap<OVXPort, SwitchRoute>> 		routeMap;
    
    /**For assigning route IDs*/
    private final AtomicInteger 						routeCounter;
    
    public OVXBigSwitch(final long switchId, final int tenantId) {
	super(switchId, tenantId);
	this.alg = RoutingAlgorithms.NONE;
	this.pathMap = new HashMap<OVXPort, HashMap<OVXPort, LinkedList<PhysicalLink>>>();
	this.routing = new ManualRoute();
	this.routeMap = new HashMap<OVXPort, HashMap<OVXPort, SwitchRoute>>();
	this.routeCounter = new AtomicInteger(0);
    }

    /**
     * Gets the alg.
     * 
     * @return the alg
     */
    public RoutingAlgorithms getAlg() {
	return this.alg;
    }

    /**
     * Sets the alg.
     * 
     * @param alg
     *            the new alg
     */
    public void setAlg(final RoutingAlgorithms alg) {
	this.alg = alg;
	this.routing = alg.getRoutable();
    }
    
    /**
     * @return The routable for this Big Switch
     */
    public Routable getRoutable() {
	return this.routing;
    }

    /**
     * @param srcPort the ingress port on the Big Switch
     * @param dstPort the egress port on the Big Switch 
     * @return list of physical links
     */
    public ArrayList<PhysicalLink> getRoute(OVXPort srcPort, OVXPort dstPort) {
	return this.routing.getRoute(this, srcPort, dstPort);    
    }
    
    public HashMap<OVXPort, HashMap<OVXPort, SwitchRoute>> getRouteMap() {
	return this.routeMap;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see net.onrc.openvirtex.elements.datapath.Switch#removePort(short)
     */
    @Override
    public boolean removePort(final Short portNumber) {
	if (!this.portMap.containsKey(portNumber)) {
	    return false;
	} else {
	    this.portMap.remove(portNumber);
	    return true;
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.onrc.openvirtex.core.io.OVXSendMsg#sendMsg(org.openflow.protocol.
     * OFMessage, net.onrc.openvirtex.core.io.OVXSendMsg)
     */
    @Override
    public void sendMsg(final OFMessage msg, final OVXSendMsg from) {
	// TODO Truncate the message for the ctrl to the missSetLenght value
	if (this.isConnected) {
	    this.channel.write(Collections.singletonList(msg));
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.onrc.openvirtex.elements.datapath.Switch#handleIO(org.openflow.protocol
     * .OFMessage)
     */
    @Override
    public void handleIO(final OFMessage msg) {
	try {
	    ((Devirtualizable) msg).devirtualize(this);
	} catch (final ClassCastException e) {
	    OVXBigSwitch.log.error("Received illegal message : " + msg);
	}

    }

    /*
     * (non-Javadoc)
     * 
     * @see net.onrc.openvirtex.elements.datapath.Switch#tearDown()
     */
    @Override
    public void tearDown() {
	// TODO: Release any acquired resources.
	this.channel.disconnect();

    }

    @Override
    public boolean boot() {
	return super.boot();
	// TODO: Start the internal routing protocol
    }

    /**
     * Gets the port.
     * 
     * @param portNumber
     *            the port number
     * @return the port instance
     */
    @Override
    public OVXPort getPort(final Short portNumber) {
	return this.portMap.get(portNumber);
    };

    @Override
    public String toString() {
	return "SWITCH:\n- switchId: " + this.switchId + "\n- switchName: "
	        + this.switchName + "\n- isConnected: " + this.isConnected
	        + "\n- tenantId: " + this.tenantId + "\n- missSendLenght: "
	        + this.missSendLen + "\n- isActive: " + this.isActive
	        + "\n- capabilities: "
	        + this.capabilities.getOVXSwitchCapabilities()
	        + "\n- algorithm: " + this.alg.getValue();
    }

    @Override
    public void sendSouth(OFMessage msg) {
	// TODO Auto-generated method stub
	
    }
    
    /**
     * Adds a path between two edge ports on the big switch 
     * @param path
     * @return the route ID of the new route
     */
    public int createRoute(final List<PhysicalLink> path) {
	//assumes list is in correct order
	final PhysicalPort srcPhyPort = path.get(0).getSrcPort();
	final PhysicalPort dstPhyPort = path.get(path.size() - 1).getDstPort();
	final OVXPort srcPort = new OVXPort(this.getTenantId(), srcPhyPort, false);
	final OVXPort dstPort = new OVXPort(this.getTenantId(), dstPhyPort, false);
	
	final int routeId = this.routeCounter.getAndIncrement();
	SwitchRoute routeEntry = new SwitchRoute(this.switchId, routeId);
	routeEntry.addRoute(path);
	
	synchronized(routeMap) {
	    HashMap<OVXPort, SwitchRoute> rtmap =  this.routeMap.get(srcPort);
	    if (rtmap == null) {
		rtmap = new HashMap<OVXPort, SwitchRoute>();
		this.routeMap.put(srcPort, rtmap);
	    }
	    rtmap.put(dstPort, routeEntry);
	}
	
	//add reverse path dst->src
	synchronized(routeMap) {
	    HashMap<OVXPort, SwitchRoute> rtmap =  this.routeMap.get(dstPort);
	    if (rtmap == null) {
		rtmap = new HashMap<OVXPort, SwitchRoute>();
		this.routeMap.put(dstPort, rtmap);
	    }
	    rtmap.put(srcPort, routeEntry);
	}
	this.log.info("Added route {}", routeEntry);
	
	return routeId;
    }
}
