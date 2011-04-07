package de.lmu.ifi.dbs.elki.algorithm.clustering.correlation;

import org.junit.Test;

import de.lmu.ifi.dbs.elki.JUnit4Test;
import de.lmu.ifi.dbs.elki.algorithm.AbstractSimpleAlgorithmTest;
import de.lmu.ifi.dbs.elki.algorithm.clustering.DBSCAN;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.model.Model;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.index.preprocessed.localpca.KNNQueryFilteredPCAIndex;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.ParameterException;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;

/**
 * Perform a full COPAC run, and compare the result with a clustering derived
 * from the data set labels. This test ensures that COPAC performance doesn't
 * unexpectedly drop on this data set (and also ensures that the algorithms
 * work, as a side effect).
 * 
 * @author Erich Schubert
 */
public class TestCOPACResults extends AbstractSimpleAlgorithmTest implements JUnit4Test {
  /**
   * Run COPAC with fixed parameters and compare the result to a golden
   * standard.
   * 
   * @throws ParameterException on errors.
   */
  @Test
  public void testCOPACResults() throws ParameterException {
    Database<DoubleVector> db = makeSimpleDatabase(UNITTEST + "correlation-hierarchy.csv", 450);

    // these parameters are not picked too smartly - room for improvement.
    ListParameterization params = new ListParameterization();
    params.addParameter(COPAC.PARTITION_ALGORITHM_ID, DBSCAN.class);
    params.addParameter(DBSCAN.EPSILON_ID, 0.02);
    params.addParameter(DBSCAN.MINPTS_ID, 50);
    params.addParameter(COPAC.PREPROCESSOR_ID, KNNQueryFilteredPCAIndex.Factory.class);
    params.addParameter(KNNQueryFilteredPCAIndex.Factory.K_ID, 15);

    COPAC<DoubleVector> copac = ClassGenericsUtil.parameterizeOrAbort(COPAC.class, params);
    testParameterizationOk(params);

    // run COPAC on database
    Clustering<Model> result = copac.run(db);

    testFMeasure(db, result, 0.842521);
    testClusterSizes(result, new int[] { 6, 16, 32, 196, 200 });
  }
}