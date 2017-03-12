package org.opendatakit.aggregate.client.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Utilities to manipulate Java Collections.
 * @author Caden Howell
 */
public class CollectionUtils {
  /**
   * Convert a collection (like a set) to a sorted list.
   * @author erickson 
   * http://stackoverflow.com/questions/740299/how-do-i-sort-a-set-to-a-list-in-java
   * @param collection
   * @return sorted list
   */
  public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> collection) {
    List<T> list = new ArrayList<T>(collection);
    java.util.Collections.sort(list);
    return list;
  }
}
