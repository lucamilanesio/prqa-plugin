/*
 * The MIT License
 *
 * Copyright 2012 Praqma.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.praqma.jenkins.plugin.prqa.graphs;

import hudson.util.ChartUtil;
import hudson.util.ColorPalette;
import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;
import java.awt.BasicStroke;
import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.praqma.jenkins.plugin.prqa.PrqaException;
import net.praqma.prqa.PRQAContext;
import net.praqma.prqa.PRQAStatusCollection;
import net.praqma.prqa.status.StatusCategory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 *
 * @author Praqma
 */
public abstract class PRQAGraph implements Serializable {
    protected List<StatusCategory> categories;
    //protected ChartUtil.NumberOnlyBuildLabel label;
    protected PRQAStatusCollection data;
    //protected DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();
    protected String title;
    protected PRQAContext.QARReportType type;
   
    public PRQAContext.QARReportType getType() {
        return type;
    }
    
    public void setType(PRQAContext.QARReportType type) {
        this.type = type; 
    }
    
    public List<StatusCategory> getCategories() {
        return categories;
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setData(PRQAStatusCollection data) {
        this.data = data;
    }
    
//    public DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> getDataSetBuilder() {
//        return dsb;
//    }
//    
//    public void setDataSetBuilder(DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb) {
//        this.dsb = dsb;
//    }
//    
//    public void setLabel(ChartUtil.NumberOnlyBuildLabel label) {
//        this.label = label;
//    }
    
    public boolean containsStatus(StatusCategory cat) {
        return categories.contains(cat);
    }
    
    public void drawGraph(StaplerRequest req, StaplerResponse rsp, DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb) throws IOException {
        Number max = null;
        Number min = null;
        int width = Integer.parseInt(req.getParameter("width"));
        int height = Integer.parseInt(req.getParameter("height"));
        
        for (StatusCategory category : categories) {
            try {
                max = data.getMax(category);
                min = data.getMin(category);                
            } catch (PrqaException.PrqaReadingException iex) {
                continue;
            }
            if(max != null && min != null)
                ChartUtil.generateGraph( req, rsp, createChart( dsb.build(), getTitle() == null ? category.toString() : getTitle() , null, max.intValue(), min.intValue()), width, height );     
        }
        

    }
    
    protected JFreeChart createChart( CategoryDataset dataset, String title, String yaxis, int max, int min ) {
        final JFreeChart chart = ChartFactory.createLineChart( title, // chart
                                                                                                                                        // title
                        null, // unused
                        yaxis, // range axis label
                        dataset, // data
                        PlotOrientation.VERTICAL, // orientation
                        true, // include legend
                        true, // tooltips
                        false // urls
        );

        final LegendTitle legend = chart.getLegend();

        legend.setPosition( RectangleEdge.BOTTOM );


        chart.setBackgroundPaint( Color.white );

        final CategoryPlot plot = chart.getCategoryPlot();

        plot.setBackgroundPaint( Color.WHITE );
        plot.setOutlinePaint( null );
        plot.setRangeGridlinesVisible( true );
        plot.setRangeGridlinePaint( Color.black );

        CategoryAxis domainAxis = new ShiftedCategoryAxis( null );
        plot.setDomainAxis( domainAxis );
        domainAxis.setCategoryLabelPositions( CategoryLabelPositions.UP_90 );
        domainAxis.setLowerMargin( 0.0 );
        domainAxis.setUpperMargin( 0.0 );
        domainAxis.setCategoryMargin( 0.0 );

        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits( NumberAxis.createIntegerTickUnits() );
        rangeAxis.setUpperBound( max );
        rangeAxis.setLowerBound( min );

        final LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setBaseStroke( new BasicStroke( 2.0f ) );
        ColorPalette.apply( renderer );

        plot.setInsets( new RectangleInsets( 5.0, 0, 0, 5.0 ) );

        return chart;
    }
   
    public PRQAGraph(String title, PRQAContext.QARReportType type, StatusCategory... category) {
        data = new PRQAStatusCollection();
        categories = new ArrayList<StatusCategory>();
        categories.addAll(Arrays.asList(category));
        this.type = type;
        this.title = title;
    }
}