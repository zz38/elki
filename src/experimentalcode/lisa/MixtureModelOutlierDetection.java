package experimentalcode.lisa;

import java.util.ArrayList;
import java.util.List;

import de.lmu.ifi.dbs.elki.algorithm.AbstractAlgorithm;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.database.AssociationID;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Matrix;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import de.lmu.ifi.dbs.elki.result.AnnotationFromDatabase;
import de.lmu.ifi.dbs.elki.result.MultiResult;
import de.lmu.ifi.dbs.elki.result.OrderingFromAssociation;
import de.lmu.ifi.dbs.elki.utilities.DatabaseUtil;
import de.lmu.ifi.dbs.elki.utilities.Description;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.DoubleParameter;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.ParameterException;
/**
 * Outlier detection algorithm using a mixture model approach.
 * The data is modeled as a mixture of two distributions, one for ordinary data and one for outliers. 
 * At first all Objects are in the  set of normal objects and the set of anomalous objects is empty. An iterative procedure then transfers 
 * objects from the ordinary set to the anomalous set if the transfer increases the overall likelihood of the data. 
 * 
 * <p>Reference:<br>  
 * Eskin, Eleazar: Anomaly detection over noisy data using learned probability distributions. 
 * In Proc. of the Seventeenth International Conference on Machine Learning (ICML-2000).
 * </p>
 * 
 * @author Lisa Reichert
 * 
 * @param <V> Vector Type
 */
public class MixtureModelOutlierDetection<V extends NumberVector<V,Double>> extends AbstractAlgorithm<V,MultiResult> {
  /**
   * The association id to associate the MMOD_OFLAF of an object for the
   * MixtureModelOutlierDetection algorithm.
   */
  public static final AssociationID<Double> MMOD_OFLAG = AssociationID.getOrCreateAssociationID("mmod.oflag", Double.class);
  /**
   * OptionID for {@link #L_PARAM}
   */
  public static final OptionID L_ID = OptionID.getOrCreateOptionID("mmo.l", "expected fraction of outliers");
  /**
   * OptionID for {@link #C_PARAM}
   */
  public static final OptionID C_ID = OptionID.getOrCreateOptionID("mmo.c", "cutoff");
/**
 * Small value to increment diagonally of a matrix
 * in order to avoid singularity before building the inverse.
 */
private static final double SINGULARITY_CHEAT = 1E-9;  

      /**
       * Parameter to specify the fraction of expected outliers,
       * 
       * <p>Key: {@code -mmo.l} </p>
       */
      private final DoubleParameter L_PARAM = new DoubleParameter(L_ID);
      /**
       * Holds the value of {@link #L_PARAM}.
       */
      private double l;
  

      /**
       * Parameter to specify the cutoff,
       * 
       * <p>Key: {@code -mmo.c} </p>
       */
      private final DoubleParameter C_PARAM = new DoubleParameter(C_ID);
      /**
       * Holds the value of {@link #L_PARAM}.
       */
      private double c;
  
      /**
       * Provides the result of the algorithm.
       */
      MultiResult result;
      
      /**
       * Constructor, adding options to option handler.
       */
      public MixtureModelOutlierDetection() {
        super();
        addOption(C_PARAM);
        addOption(L_PARAM);  
      }
      /**
       * Calls the super method
       * and sets additionally the values of the parameter
       * {@link #K_PARAM}
       */
      @Override
      public List<String> setParameters(List<String> args) throws ParameterException {
          List<String> remainingParameters = super.setParameters(args);

          // l fraction of expected outliers
          l = L_PARAM.getValue();
          //cutoff
          c = C_PARAM.getValue();
          return remainingParameters;
      }
  
  @Override
  protected MultiResult runInTime(Database<V> database) throws IllegalStateException {
  
    //set of normal objects (containing all data in the beginning) and a 
    List<Integer> normalObjs = database.getIDs();
    //set of anomalous objects(empty in the beginning) 
    List<Integer> anomalousObjs = new ArrayList<Integer>();
    //compute loglikelihood
    double logLike = database.size()*Math.log(1-l) + loglikelihoodNormal(normalObjs, database);
    debugFine("normalsize   " + normalObjs.size()+ " anormalsize  " + anomalousObjs.size() + " all " + (anomalousObjs.size()+normalObjs.size()) );
    debugFine(logLike +" loglike beginning" + loglikelihoodNormal(normalObjs, database));
    for(int i= 0; i< normalObjs.size()&& normalObjs.size() > 0; i++){
      debugFine("i     " + i);
      //move object to anomalousObjs and test if the loglikelyhood increases significantly
      Integer x = normalObjs.get(i);
      anomalousObjs.add(x);
      normalObjs.remove(i);
      double currentLogLike = normalObjs.size()*Math.log(1-l) + loglikelihoodNormal(normalObjs, database) + anomalousObjs.size()*Math.log(l) + loglikelihoodAnomalous(anomalousObjs);
 
      double deltaLog =Math.abs(logLike- currentLogLike) ;

      //if the loglike increases more than a threshold, object stays in anomalous set and is flagged as outlier
      if(deltaLog > c && currentLogLike > logLike) {
        //flag as outlier 
        debugFine("outlier id" + x);
        database.associate(MMOD_OFLAG, x, 1.0);
        logLike = currentLogLike;
        i--;
      }
      else{
        //move object back to normalObjects
        normalObjs.add(i, x);
        debugFine("nonoutlier id" + x);
        anomalousObjs.remove(anomalousObjs.size()-1);
        //flag as non outlier
        database.associate(MMOD_OFLAG, x, 0.0);
        
      }
      
    }
    
    AnnotationFromDatabase<Double, V> res1 = new AnnotationFromDatabase<Double, V>(database, MMOD_OFLAG);
    // Ordering
    OrderingFromAssociation<Double, V> res2 = new OrderingFromAssociation<Double, V>(database, MMOD_OFLAG, true); 
    //combine results.
   result = new MultiResult();
    result.addResult(res1);
    result.addResult(res2); 
    return result;   
  }
  /**
   *loglikelihood anomalous objects. normal distribution
   * @param anomalousObjs
   * @return
   */
  private double loglikelihoodAnomalous(List<Integer> anomalousObjs) {
    int n = anomalousObjs.size();
    
    double prob = n*Math.log( Math.pow(1.0/n,n));   

    return prob;
  }
  /**
   * * computes the loglikelihood of all normal objects. gaussian model
   * 
   * @param normalObjs
   * @return
   */
  private double loglikelihoodNormal(List<Integer> normalObjs, Database<V> database) {
    double prob = 0;
    if (normalObjs.isEmpty()){
      return prob;
    }
    else {
      V mean = DatabaseUtil.centroid(database, normalObjs);
      V meanNeg = mean.negativeVector();
      
      Matrix covarianceMatrix = DatabaseUtil.covarianceMatrix(database, normalObjs);
      Matrix covInv;
      
      //test singulaere matrix
     covInv = covarianceMatrix.cheatToAvoidSingularity(SINGULARITY_CHEAT).inverse();
      
      
      double covarianceDet = covarianceMatrix.det();
      double fakt = (1.0/(Math.sqrt(Math.pow(2*Math.PI, database.dimensionality())*(covarianceDet)))); 
      //for each object compute probability and sum
       for (Integer id: normalObjs) {
               V x = database.get(id);
               Vector x_minus_mean = x.plus(meanNeg).getColumnVector();
               double mDist = x_minus_mean.transposeTimes(covInv).times(x_minus_mean).get(0,0);
               prob += Math.log(fakt * Math.exp(- mDist/2.0));
               
       }
       return prob;
    }
  }
  
  @Override
  public Description getDescription() {
    return new Description(
        "Mixture Model",
        "Mixture Model Outlier Detection",
        "sd",
        "Eskin, Eleazar: Anomaly detection over noisy data using learned probability distributions. +" +
        "In: Proc. of the Seventeenth International Conference on Machine Learning (ICML-2000).");
  }
  @Override
  public MultiResult getResult() {
    return result;
  }

}
