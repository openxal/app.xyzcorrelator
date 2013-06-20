/*
 * TimingDataManager.java
 *
 * Created on Thu Feb 26 11:40:08 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.smf.data;

import xal.tools.data.*;
import xal.tools.xml.*;
import xal.smf.*;


/**
 * TimingDataManager parses the XML timing source to update the TimingCenter with the 
 * timing information.  Typically there is one TimingDataManager per XMLDataManager.
 *
 * @author  tap
 */
class TimingDataManager {
	protected String _urlSpec;
	protected TimingCenter _timingCenter;
	protected String timingSchema = "xdxf.xsd";
	
	/**
	 * TimingDataManager constructor
	 * @param urlSpec The URL spec of the timing data source
	 */
	public TimingDataManager(String urlSpec) {
		_urlSpec = urlSpec;
		_timingCenter = null;
	}
	
	
	/**
	 * TimingDataManager constructor
	 */
	public TimingDataManager() {
		this(null);
	}
	
	
	/**
	 * Set the timing source's URL
	 * @param urlSpec The new URL spec of the timing data source
	 */
	public void setURLSpec(String urlSpec) {
		_urlSpec = urlSpec;
		_timingCenter = null;
	}
	
	
	/**
	 * Get the timing center generated from this manager's timing data source
	 * @return the timing center generated from this manager's timing data source
	 */
	public TimingCenter getTimingCenter() {
		return (_timingCenter != null) ? _timingCenter : parseTimingCenter();
	}
	
	
	/**
	 * Parse the timing data source to get the timing center
	 * @return The timing center parsed from this manager's timing data source
	 */
	protected TimingCenter parseTimingCenter() {
		TimingCenter timingCenter = new TimingCenter();
		updateTimingCenter(timingCenter);
		return timingCenter;
	}
	
	
	/**
	 * Update the specified timing center with data from the timing data source
	 * @param timingCenter The timing center to update
	 */
	protected void updateTimingCenter(TimingCenter timingCenter) {
		if ( _urlSpec != null ) {
			XmlDataAdaptor documentAdaptor = XmlDataAdaptor.adaptorForUrl(_urlSpec, false,
					XMLDataManager.getDefaultInstance().absoluteUrlSpec(timingSchema));
			DataAdaptor timingAdaptor = documentAdaptor.childAdaptor(TimingCenter.DATA_LABEL);
			timingCenter.update(timingAdaptor);
		}
	}
	
	
	/**
	 * Write the timing center to the specified URL
	 * @param urlSpec The URL where we will write the timing XML
	 * @param dtdURISpec The URI spec of the DTD used for validating the XML
	 */
	private void writeTimingCenterToURL(final String urlSpec, final String dtdURISpec) {
		XmlDataAdaptor adaptor = XmlDataAdaptor.newDocumentAdaptor(_timingCenter, dtdURISpec);
		adaptor.writeToUrlSpec(urlSpec);
	}
	
	
	/**
	 * Write the timing center to the URL of this TimingDataManager
	 * @param dtdURISpec The URI spec of the DTD used for validating the XML
	 */
	private void writeTimingCenter(final String dtdURISpec) {
		writeTimingCenterToURL(_urlSpec, dtdURISpec);
	}
}

