package il.ac.technion.cs.ssdl.spartan.refactoring;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Boris van Sosin
 * 
 * @since 2013/07/01
 */
public enum All {
  ;
  private static final Map<String, BaseSpartanization> all = new HashMap<String, BaseSpartanization>();
  
  private static void put(final BaseSpartanization s) {
    all.put(s.toString(), s);
  }
  
  static {
    put(new ComparisonWithBoolean());
    put(new Ternarize());
    put(new ShortestBranch());
    put(new InlineSingleUse());
    put(new ForwardDeclaration());
    put(new RenameReturnToDollar());
  }
  
  /**
   * @param name
   *          the name of the spartanization
   * @return an instance of the spartanization
   */
  public static BaseSpartanization get(final String name) {
    return all.get(name);
  }
  
  /**
   * @return all the registered spartanization refactoring objects
   */
  public static Iterable<BaseSpartanization> all() {
    return all.values();
  }
}
