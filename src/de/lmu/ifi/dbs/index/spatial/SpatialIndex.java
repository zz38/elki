package de.lmu.ifi.dbs.index.spatial;

import de.lmu.ifi.dbs.data.RealVector;
import de.lmu.ifi.dbs.utilities.QueryResult;

import java.util.List;

/**
 * Defines the requirements for a spatial index that can be used to efficiently store data.
 *
 * @author Elke Achtert (<a href="mailto:achtert@dbs.ifi.lmu.de">achtert@dbs.ifi.lmu.de</a>)
 */
public interface SpatialIndex<T extends RealVector> {

  /**
   * Inserts the specified reel vector object into this index.
   *
   * @param o the vector to be inserted
   */
  void insert(T o);

  /**
   * Deletes the specified obect from this index.
   *
   * @param o the object to be deleted
   * @return true if this index did contain the object, false otherwise
   */
  boolean delete(T o);

  /**
   * Performs a range query for the given RealVectorc with the given
   * epsilon range and the according distance function.
   * The query result is in ascending order to the distance to the
   * query object.
   *
   * @param obj              the query object
   * @param epsilon          the string representation of the query range
   * @param distanceFunction the distance function that computes the distances beween the objects
   * @return a List of the query results
   */
  List<QueryResult> rangeQuery(final T obj, final String epsilon,
                               final SpatialDistanceFunction<T> distanceFunction);

  /**
   * Performs a k-nearest neighbor query for the given RealVector with the given
   * parameter k and the according distance function.
   * The query result is in ascending order to the distance to the
   * query object.
   *
   * @param obj              the query object
   * @param k                the number of nearest neighbors to be returned
   * @param distanceFunction the distance function that computes the distances beween the objects
   * @return a List of the query results
   */
  List<QueryResult> kNNQuery(final T obj, final int k,
                             final SpatialDistanceFunction<T> distanceFunction);

//  IndexableIterator dataIterator();

  /**
   * Returns the IO-Access of this index.
   *
   * @return the IO-Access of this index
   */
  long getIOAccess();

//  void resetIOAccess();

//  SpatialNode getNode(int nodeID);

  /**
   * Returns the root of this index.
   *
   * @return the root of this index
   */
  SpatialNode getRoot();

//  LeafIterator leafIterator();

  /**
   * Returns a list of the leaf nodes of this spatial index.
   *
   * @return a list of the leaf nodes of this spatial index
   */
  List<SpatialNode> getLeafNodes();

  /**
   * Returns the spatial node with the specified ID.
   *
   * @param nodeID the id of the node to be returned
   * @return the spatial node with the specified ID
   */
  SpatialNode getNode(int nodeID);

  /**
   * Returns the entry that denotes the root.
   * @return the entry that denotes the root
   */
  Entry getRootEntry();
}
