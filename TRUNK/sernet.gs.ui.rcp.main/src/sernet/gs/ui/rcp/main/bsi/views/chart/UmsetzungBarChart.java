package sernet.gs.ui.rcp.main.bsi.views.chart;

import java.awt.Color;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.MassnahmenSummaryHome;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;

public class UmsetzungBarChart implements IChartGenerator {
	
	

	public JFreeChart createChart() {
		return createBarChart(createBarDataset());
	}
	
	protected JFreeChart createBarChart(Object dataset) {
		JFreeChart chart = ChartFactory.createStackedBarChart3D(null,
				"Umsetzung", "Maßnahmen", (CategoryDataset) dataset,
				PlotOrientation.HORIZONTAL, false, true, false);
		chart.setBackgroundPaint(Color.white);
		chart.getPlot().setForegroundAlpha(0.6f);
		chart.setBackgroundPaint(Color.white);
		CategoryPlot plot = (CategoryPlot) chart.getPlot();

		plot.getDomainAxis().setCategoryLabelPositions(
				CategoryLabelPositions.STANDARD);
		return chart;

	}

	protected Object createBarDataset() {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		MassnahmenSummaryHome dao = new MassnahmenSummaryHome();

		Map<String, Integer> items = dao.getUmsetzungenSummary();
		Set<Entry<String, Integer>> entrySet = items.entrySet();
		for (Entry<String, Integer> entry : entrySet) {
			dataset.addValue(entry.getValue(), getLabel(entry.getKey()),
					getLabel(entry.getKey()));
		}
		return dataset;
	}
	
	private String getLabel(String key) {
		PropertyType type = HUITypeFactory.getInstance().getPropertyType(
				MassnahmenUmsetzung.TYPE_ID, MassnahmenUmsetzung.P_UMSETZUNG);
		if (type == null || type.getOption(key) == null)
			return "unbearbeitet";
		return type.getOption(key).getName();
	}

}
