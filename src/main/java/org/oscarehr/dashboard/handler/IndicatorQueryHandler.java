/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */
package org.oscarehr.dashboard.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.oscarehr.dashboard.display.beans.GraphPlot;
import org.oscarehr.util.MiscUtils;


/**
 * Depends on a proper convention of place-holders in the query text.
 * 
 * RegEx is:  "(\\$){1}(\\{){1}( )*(.)*( )*(\\}){1}"
 * ie: ${ parameter_id }
 *
 */
public class IndicatorQueryHandler extends AbstractQueryHandler {

	private static Logger logger = MiscUtils.getLogger();
	private List< GraphPlot[] > graphPlots;
	private static final Double DEFAULT_DENOMINATOR = 100.0;

	public IndicatorQueryHandler() {	
		// default
	}

	public List<?> execute( String query ) {

		logger.info("Executing Indicator Query");

		this.setQuery( query );	
		List<?> results = super.execute();
		setGraphPlots( results );
		return results;
	}

	public void setQuery( String query ) {
		String finalQuery = super.buildQuery( query );
		super.setQuery( finalQuery );
	}

	public List<GraphPlot[]> getGraphPlots() {
		return graphPlots;
	}

	/**
	 * Build graph data with GraphPlot objects and set them into the Indicator Bean for display.
	 * Each row of graph plots is a new graph
	 * Each GraphPlot column is a plot on the graph. 
	 */
	@SuppressWarnings("unchecked")
	protected void setGraphPlots( List<?> results ) {

		List< GraphPlot[] > graphPlotList = null;

		for(Object row : results) {
			if( graphPlotList == null ) {
				graphPlotList = new ArrayList< GraphPlot[] >();
			}

			GraphPlot[] graphPlots = createGraphPlots( (Map<String, ?>) row ); 

			graphPlotList.add( graphPlots );
		}

		this.graphPlots = graphPlotList;
	}

	/**
	 * Value is the query result and key is the column (or result) alias
	 */
	private static GraphPlot[] createGraphPlots( Map<String, ?> row ) {

		List<GraphPlot> graphPlots = null; 
		Iterator<?> it = row.keySet().iterator();
		
		while( it.hasNext() ) {
			
			if( graphPlots == null ) {
				graphPlots = new ArrayList<GraphPlot>(); 
			}
			
			GraphPlot graphPlot = new GraphPlot();
			String key = it.next() + "";
			Object value = null;

			if( key.equalsIgnoreCase("null") ) {
				key = "";
			}
			
			key = key.trim();
			
			if( ! key.isEmpty() ) {
				value = row.get( key );
			} else {
				logger.warn( "Null or Empty Key found for the label parameter of this graph plot." );
			}
			
			// Only pie charts for now - so the demon is out of 100 percent.
			graphPlot.setDenominator( DEFAULT_DENOMINATOR );

			if( value instanceof Number ) {
				Number plot = (Number) value;
				graphPlot.setNumerator( plot.doubleValue() );
			}

			graphPlot.setKey( key );
			graphPlot.setLabel( key );			
			graphPlots.add( graphPlot );
		}
		
		GraphPlot[] graphPlotArray = new GraphPlot[ graphPlots.size() ];
		graphPlots.toArray( graphPlotArray );
		
		return graphPlotArray;
	}

}