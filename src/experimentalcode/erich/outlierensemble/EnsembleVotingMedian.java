package experimentalcode.erich.outlierensemble;

import java.util.Arrays;
import java.util.List;

import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.IntervalConstraint;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.DoubleParameter;

/**
 * Simple combination rule, by taking the median. Note: median is similar to a
 * majority voting!
 * 
 * @author Erich Schubert
 */
public class EnsembleVotingMedian implements EnsembleVoting {
  /**
   * Option ID for the quantile
   */
  public final static OptionID QUANTILE_ID = OptionID.getOrCreateOptionID("ensemble.median.quantile", "Quantile to use in median voting.");

  /**
   * Quantile parameter
   */
  private final DoubleParameter QUANTILE_PARAM = new DoubleParameter(QUANTILE_ID, new IntervalConstraint(0.0, IntervalConstraint.IntervalBoundary.CLOSE, 1.0, IntervalConstraint.IntervalBoundary.CLOSE), 0.5);

  /**
   * Quantile to use
   */
  private double quantile = 0.5;

  /**
   * Constructor, adhering to
   * {@link de.lmu.ifi.dbs.elki.utilities.optionhandling.Parameterizable}
   * 
   * @param config Parameterization
   */
  public EnsembleVotingMedian(Parameterization config) {
    if(config.grab(QUANTILE_PARAM)) {
      quantile = QUANTILE_PARAM.getValue();
    }
  }

  @Override
  public double combine(List<Double> scores) {
    Double[] s = scores.toArray(new Double[] {});
    Arrays.sort(s);
    double pos = quantile * s.length;
    int u = Math.min(s.length, (int) Math.ceil(pos));
    int l = Math.max(0, (int) Math.floor(pos));
    if(u == l) {
      return s[u];
    }
    else {
      // weighted, for quantiles != 0.5 this can be other than 0.5
      double wu = u - pos;
      double wl = pos - l;
      return s[u] * wu + s[l] * wl;
    }
  }
}