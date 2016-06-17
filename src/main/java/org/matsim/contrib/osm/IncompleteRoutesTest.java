package org.matsim.contrib.osm;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.gui.dialogs.relation.DownloadRelationMemberTask;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;

public class IncompleteRoutesTest extends Test{

	/**
	 * Lists incomplete routes.
	 */
	private ArrayList<Relation> incompleteRoutes;

	/**
	 * Integer code for incomplete routes.
	 */
	private final static int ROUTE_INCOMPLETE= 3010;

	/**
	 * /** Creates a new {@code MATSimTest}.
	 */
	public IncompleteRoutesTest() {
		super(tr("IncompleteRoutesTest"), tr("IncompleterRoutesTest"));
	}

	/**
	 * Starts the test. Initializes the mappings of {@link #nodeIds} and
	 * {@link #linkIds}.
	 */
	@Override
	public void startTest(ProgressMonitor monitor) {
		this.incompleteRoutes = new ArrayList<>();
		super.startTest(monitor);
	}


	@Override
	public void visit(Relation r) {

		if (r.hasIncompleteMembers() && r.hasTag("type", "route")) {
			incompleteRoutes.add(r);
		}

	}


	/**
	 * Ends the test. Errors and warnings are created in this method.
	 */
	@Override
	public void endTest() {

		for(Relation relation: incompleteRoutes) {
			String msg = ("Incomplete route "+relation.get("ref")+" - Auto repair to download missing elements (cannot be undone)");
			errors.add(new TestError(this, Severity.WARNING, msg,
					ROUTE_INCOMPLETE, relation));
		}
		super.endTest();
	}

	@Override
	public boolean isFixable(TestError testError) {
		return testError.getCode() == ROUTE_INCOMPLETE;
	}

	@Override
	public Command fixError(TestError testError) {
		if (!isFixable(testError)) {
			return null;
		}
		if (testError.getCode() == 3010) {
			for(OsmPrimitive primitive: testError.getPrimitives()) {
				Main.worker.submit(new DownloadRelationMemberTask((Relation) primitive, ((Relation) primitive).getIncompleteMembers(), Main.getLayerManager().getEditLayer()));
			}

		}
		return null;
	}
}
