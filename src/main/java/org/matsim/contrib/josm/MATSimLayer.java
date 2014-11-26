package org.matsim.contrib.josm;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JOptionPane;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.RenameLayerAction;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

/**
 * a layer which contains MATSim-network data to differ from normal OSM layers
 * 
 * @author nkuehnel
 * 
 */
class MATSimLayer extends OsmDataLayer {
	private final Scenario matsimScenario;

    // data mappings
	private Map<Way, List<Link>> way2Links = new HashMap<>();
	private Map<Link, List<WaySegment>> link2Segment = new HashMap<>();
	private Map<Relation, TransitRoute> relation2Route = new HashMap<>();
	private Map<TransitStopFacility, Id<TransitStopFacility>> facility2OrigId = new HashMap<>(); 

    public MATSimLayer(DataSet data, String name, File associatedFile,
                       Scenario scenario,
                       HashMap<Way, List<Link>> way2Links,
                       Map<Link, List<WaySegment>> link2Segment,
                       Map<Relation, TransitRoute> relation2Route, Map<TransitStopFacility, Id<TransitStopFacility>> facility2OrigId) {
		super(data, name, associatedFile);
		this.matsimScenario = scenario;
        this.way2Links = way2Links;
		this.link2Segment = link2Segment;
		this.relation2Route = relation2Route;
		this.facility2OrigId = facility2OrigId;

		// attach listener to layer
		NetworkListener listener;
		try {
			listener = new NetworkListener(scenario, way2Links, link2Segment,
					relation2Route);
		} catch (IllegalArgumentException e) { // create layer even if no
												// listener can be attached
			JOptionPane
					.showMessageDialog(
							Main.parent,
							"Could not initialize network listener with the given coordinate system.\nChanges on layer data will NOT affect the network.",
							tr("Error"), JOptionPane.ERROR_MESSAGE);
			listener = null;
		}
		if (listener != null) {
			data.addDataSetListener(listener);
		}
	}

	public Map<Way, List<Link>> getWay2Links() {
		return way2Links;
	}

	public Map<Link, List<WaySegment>> getLink2Segments() {
		return link2Segment;
	}

	public Map<Relation, TransitRoute> getRelation2Route() {
		return relation2Route;
	}

	public Scenario getMatsimScenario() {
		return matsimScenario;
	}
	
	public Map<TransitStopFacility, Id<TransitStopFacility>> getFacility2OrigId() {
		return facility2OrigId;
	}

	@Override
	public Action[] getMenuEntries() {
//		if (Main.applet)
//			return new Action[] {
//					LayerListDialog.getInstance().createActivateLayerAction(
//							this),
//					LayerListDialog.getInstance().createShowHideLayerAction(),
//					LayerListDialog.getInstance().createDeleteLayerAction(),
//					SeparatorLayerAction.INSTANCE,
//					new RenameLayerAction(getAssociatedFile(), this),
//					SeparatorLayerAction.INSTANCE,
//					new LayerListPopup.InfoAction(this) };
		List<Action> actions = new ArrayList<>();
		actions.addAll(Arrays.asList(LayerListDialog.getInstance().createActivateLayerAction(this),
                LayerListDialog.getInstance().createShowHideLayerAction(),
                LayerListDialog.getInstance().createDeleteLayerAction(),
                SeparatorLayerAction.INSTANCE, new LayerSaveAsAction(this)));
		actions.addAll(Arrays.asList(SeparatorLayerAction.INSTANCE,
                new RenameLayerAction(getAssociatedFile(), this)));
		actions.addAll(Arrays.asList(SeparatorLayerAction.INSTANCE,
                new LayerListPopup.InfoAction(this)));
		return actions.toArray(new Action[actions.size()]);
	}

	@Override
	public boolean isMergable(final Layer other) {
		return false;
	}

	public boolean requiresUploadToServer() {
		return false;
	}
}
