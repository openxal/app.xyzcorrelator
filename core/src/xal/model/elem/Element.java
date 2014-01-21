/*
 * Element.java
 *
 * Created on August 11, 2002, 8:43 AM
 *
 */

package xal.model.elem;



import java.io.PrintWriter;
import java.util.ArrayList;

import xal.model.IElement;

import xal.tools.beam.IConstants;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.math.r3.R3;

import xal.model.IModelDataSource;
import xal.model.IProbe;
import xal.model.IAlgorithm;
import xal.model.ModelException;
import xal.model.alg.Tracker;



/**
 * Convenience abstract base class for constructing XAL modeling elements.
 * This class implements many of the general methods for the 
 * <code>IElement</code> interface that are not element specific.
 *
 * @author  Christopher Allen
 * @author Craig McChesney
 */

public abstract class Element implements IElement {

    
    /*
     *  Global Attributes
     */
    
    /** Global index counter for object identifiers */
    private static  int     s_cntInstances;
    
    
    /*
     *  Local Attributes
     */
    
    /** internal unique identifier of element */
    private int         m_intUID;
    
    /** the element type identifier */
    private String      m_strType;
    
    /** element instance identifier of element */
    private String      m_strId;
    
//  sako
    //position in s (m)
    /** This is the center position of the element with the lattice - CKA */
    private double      dblLatPos;
    
    //sako closeElements (for fringe field calculations)
    /** 
     * This is a containger of nearest-neighbor elements used for computing
     * transfer maps in the presence of permanent magnet quadrupoles.
     */
    private ArrayList<Element> closeElements = null;
  
    
    //hs alignment
    private double alignx = 0.0;
    private double aligny = 0.0;
    private double alignz = 0.0;
    
    
  
    
    /*
     *  Class loader initialization
     */
    static {
        s_cntInstances = 0;
    };
    
    
    /*
     *  Abstract Methods
     */
    
    /**
     *  Return the length of this element.  Derived class must
     *  implement this because it is undetermined whether or not this is a thin
     *  or thick element.
     */
    public abstract double getLength();
    
    /**
     * Returns the time taken for the probe <code>probe</code> to propagate 
     * through a subsection of the element with length <code>dblLen</code>.
     * 
     *  @param  probe   determine energy gain for this probe
     *  @param  dblLen  length of subsection to calculate energy gain for
     *  
     *  @return         the elapsed time through section<bold>Units: seconds</bold> 
     */
    public abstract double elapsedTime(IProbe probe, double dblLen);
    
    /** 
     *  Returns energy gain for <b>subsection</b> of this element of length 
     *  <code>dblLen</code> for the specified given probe.
     *
     *  @param  probe   determine energy gain for this probe
     *
     *  @return         the energy gain provided by this element <bold>Units: eV</bold> 
     */
    public abstract double energyGain(IProbe probe, double dblLen);

    /**
     *  Compute the transfer matrix for <b>subsection</b> of this element of length 
     *  <code>dblLen</code> for the specified given probe.  That is, this method should 
     *  return the incremental transfer matrix.
     *
     *  @param  dblLen      length of sub-element
     *  @param  probe       probe containing parameters for the sub-sectional transfer matrix
     *
     *  @return             transfer map for an element of length dblLen
     *
     *  @exception  ModelException    unable to compute transfer map
     *
     *  @see    xal.model.IElement#transferMap(IProbe,double)
     */
    public abstract PhaseMap transferMap(IProbe probe, double dblLen) throws ModelException;   



    /*
     * Initialization
     */
                
    /**
     *  Creates a new instance of Element
     *
     *  @param  strType     type identifier of the element
     */
    public Element(String strType)    {
        this(strType, "NULLID");
    };
    
    /**
     *  Creates a new instance of Element
     *
     *  @param  strType     type identifier of the element
     *  @param  strId       string identifier of the element
     */
    public Element(String strType, String strId)    {
        m_intUID  = s_cntInstances++;
        m_strType = strType;
        m_strId   = strId;
        dblLatPos = 0.0;
    };
    
    /**
     * Initialize this Element from the supplied object.  Subclasses should
     * override this method as appropriate, e.g., cast the source to some expected
     * type and then retrieve values from it.
     * 
     * @param source object to initialize element from
     * 
     * @throws ModelException if error initializing from source
     */
    public void initializeFrom(IModelDataSource source) throws ModelException {
    }
       
    /**
     *  Set the string identifier for the element.
     *
     *  @param  strId       new string identifier for element
     */
    public void setId(String strId) {
        m_strId = strId;
    };

    
    /**
     * Set the center position of the element with the containing
     * lattice.
     * 
     * @param dblPos    center position along the design trajectory (meters) 
     */
    public void setPosition(double dblPos) {
        dblLatPos = dblPos;
    }
    
    
    /**
     * Set the alignment parameters all at once.
     * 
     * @param vecAlign  (dx,dy,dz)
     * 
     * @author Christopher K. Allen
     */
    public void setAlign(R3 vecAlign)    {
        alignx = vecAlign.getx();
        aligny = vecAlign.gety();
        alignz = vecAlign.getz();
    }
    
    
    public void setAlignX(double x) {
        alignx = x;
    }
    public void setAlignY(double y) {
        aligny = y;
    }
    public void setAlignZ(double z) {
        alignz = z;
    }
    
    public double getAlignX() {
        return alignx;
    }
    public double getAlignY() {
        return aligny;
    }
    public double getAlignZ() {
        return alignz;
    }
    
    /**
     * Add an element to the list of nearest neighbor elements used when
     * considering the effects of PMQs
     * 
     * @param closeElem     an adjacent element
     */
    public void addCloseElements(Element closeElem) {
        if (closeElements == null) {
            closeElements = new ArrayList<Element>();
        }
        closeElements.add(closeElem);
    }
    
    
    /*
     *  Property Queries
     */
    
    /** 
     *  Return the internal class unique identifier of this element.
     *
     *  @return     the unique identifier of this object
     */
    public int  getUID()  { return m_intUID; };
    

    
    
    /*
     * Dynamic Parameters
     */
    
    
    /**
     * Returns the given transfer matrix adjusted by the
     * misalignment parameters of this element.
     *
     * @param matPhi    a transfer matrix
     * 
     * @return          the given transfer matrix modified to contain misalignment errors
     *
     * @author Christopher K. Allen
     * @since  Apr 13, 2011
     */
    protected PhaseMatrix applyAlignError(PhaseMatrix matPhi) {

        double delx = getAlignX();
         double dely = getAlignY();
         double delz = getAlignZ();
         
         if ((delx==0)&&(dely==0)&&(delz==0)) {
             return matPhi; // do nothing
         }

         PhaseMatrix T = new PhaseMatrix();
         
         for (int i=0;i<7;i++) {
             T.setElem(i,i,1);
         }
         
         T.setElem(0,6,-delx);
         T.setElem(2,6,-dely);
         T.setElem(4,6,-delz);
         PhaseMatrix Phidx = T.inverse().times(matPhi).times(T);

         return Phidx;
     }
     
     
    /**
     * <p>This method is intended to return the location of the probe within
     * the current element.  Actually, we compute and return the distance of 
     * the probe from the element entrance location.
     * </p>
     * 
     * NOTE
     * <p> This method will always return a value regardless of 
     * whether or not the probe is within the element domain.  The
     * returned value is the distance fromt the entrance location.
     * Thus, negative values indicate an upstream position and values
     * larger than the element length indicate positions downstream.
     * </p>
     * 
     * @param   probe   probe within element domain
     * 
     * @return  the distance within the element entrance
     * 
     * @author Christopher K. Allen
     */
    public double  compProbeLocation(IProbe probe) {
        
        double lenElem = this.getLength();      // element length
        double sCenter = this.getPosition();    // center position w/in lattice
        
        double sProbe  = probe.getPosition();   // probe position with lattice
        
        double sElem = sProbe - (sCenter - lenElem/2.0);
        
        return sElem;
    }
    
    
    /*
     *  IElement Interface
     */
    
    /**
     *  Return the element type identifier
     *
     *  @return     element type string
     */
    public String   getType()   { return m_strType; };
    
    /**
     *  Returns the string identifier for this element.
     *
     *  @return     string identifier
     */
    public String   getId()     { return m_strId; };
    
    /** 
     * <p>
     * Override of {@link xal.model.IComponent#propagate(xal.model.IProbe, double)}
     * Propagates the Probe object through this element based on the associated algorithm.
     * </p>  
     *  
     *  <p>NOTE: CKA
     *  <br/>
     *  The position of the probe within the element appears to be kept as a
     *  field of the algorithm object.  I am not exactly sure of any 
     *  side-effects of this implementation when using the
     *  <code>{@link xal.model.alg.Tracker#propagate(IProbe, IElement)}</code> of the 
     *  <code>{@link xal.model.alg.Tracker}</code> class.  Careful when modifying.
     *  </p>
     *
     *  @param  probe       probe object to propagate
     *  @param  pos         I think it is position of the probe within this element
     *
     *  @exception  ModelException    error occurred during propagation
     * 
     *  @see xal.model.IComponent#propagate(xal.model.IProbe, double)
     *  @see xal.model.alg.Tracker#propagate(IProbe, IElement)
     */
    public void propagate(IProbe probe, double pos) throws ModelException {
        
        IAlgorithm      alg;    // algorithm for the probe
        
        alg = probe.getAlgorithm();
        if (alg instanceof Tracker) {
        	Tracker tracker = (Tracker)alg;
        	System.out.println("tracker.setElemPosition to "+pos);
        	tracker.setElemPosition(pos);
        }
        alg.propagate(probe, this);
    };

    
    /** 
     * <p>
     * Override of {@link xal.model.IComponent#propagate(xal.model.IProbe, double)}
     * Propagates the Probe object through this element based on the associated algorithm.
     * </p>  
     *
     *  @param  probe       probe object to propagate
     *
     *  @exception  ModelException    error occurred during propagation
     *  
     *  @see xal.model.IComponent#propagate(xal.model.IProbe, double)
     */
    public void propagate(IProbe probe) throws ModelException {
        
        IAlgorithm      alg;    // algorithm for the probe
        
        alg = probe.getAlgorithm();
        if (alg instanceof Tracker) {
        	Tracker tracker = (Tracker)alg;
        	tracker.setElemPosition(0);
        }
        alg.propagate(probe, this);
    };

    
    /** 
     *  <p>
     *  Back propagates the Probe object through this element 
     *  based on the associated algorithm.
     *  </p>
     * <p>
     * <strong>NOTES</strong>: CKA
     *  <br/>
     *  The position of the probe within the element appears to be kept as a
     *  field of the algorithm object.  I am not exactly sure of any 
     *  side-effects of this implementation when using the
     *  <code>{@link Tracker#propagate(IProbe, IElement)}</code> of the 
     *  <code>{@link Tracker}</code> class.  Careful when modifying.
     * <br/>
     * &middot; Support for backward propagation
     * February, 2009.
     * <br/>
     * &middot; You must use the <em>proper algorithm</em> object
     * for this method to work correctly!
     * </p>
     * 
     *  </p>
     *
     *  @param  probe       probe object to propagate
     *  @param  pos         I think it is position of the probe within this element
     *
     *  @exception  ModelException    error occurred during propagation
     * 
     *  @see xal.model.IComponent#propagate(xal.model.IProbe, double)
     *  @see xal.model.alg.Tracker#propagate(IProbe, IElement)
     */
    public void backPropagate(IProbe probe, double pos) throws ModelException {
        
        IAlgorithm      alg;    // algorithm for the probe
        
        alg = probe.getAlgorithm();
        if (alg instanceof Tracker) {
        	Tracker tracker = (Tracker)alg;
        	System.out.println("tracker.setElemPosition to "+pos);
        	tracker.setElemPosition(pos);
        }
        alg.propagate(probe, this);
    }


    /** 
     * <p>
     * Back propagates the Probe object through this element based on the 
     * associated algorithm.
     * </p>  
     *
     * <p>
     * <strong>NOTES</strong>: CKA
     * <br/>
     * &middot; Support for backward propagation
     * February, 2009.
     * <br/>
     * &middot; You must use the <em>proper algorithm</em> object
     * for this method to work correctly!
     * </p>
     * 
     *  @param  probe       probe object to propagate
     *
     *  @exception  ModelException    error occurred during propagation
     *  
     *  @see xal.model.IComponent#propagate(xal.model.IProbe, double)
     */
    public void backPropagate(IProbe probe) throws ModelException {
        
        IAlgorithm      alg;    // algorithm for the probe
        
        alg = probe.getAlgorithm();
        if (alg instanceof Tracker) {
            Tracker tracker = (Tracker)alg;
    
            // set position at the exit of the element
            double pos = this.getLength();
        	tracker.setElemPosition(pos);
        }
        alg.propagate(probe, this);
    }


    /**
     * Return the center position of the element along the design trajectory.
     * This is the position with the containing lattice.
     * 
     * @return  center position of the element (meters)
     */
    public double getPosition() {
        return dblLatPos;
    }

    /**
     * Return the list of nearest adjacent elements to this element.
     * THis is used primarily in permenant magnet quadrupole considerations.
     * 
     * @return  List of adjacent modeling elements
     */
    public ArrayList<Element> getCloseElements() {
        return closeElements;
    }
    

    
    
    
    
    /*
     * Subclass Support 
     */
     
    /** 
     * Compute the time the probe <code>probe</code> spends drifting a
     * a distance <code>dblLen</code>.
     *  
     * @param   probe       interface to drifting probe
     * @param   dblLen      length of drift in <b>meters</b>  
     * 
     * @return              time interval during drift in <b>seconds</b>
     */
    
    public double compDriftingTime(IProbe probe, double dblLen) {

        double dblTime = 0.0;                // the time interval
        double dblBeta = probe.getBeta();    // normalized probe velocity
     
        dblTime = dblLen / (IConstants.LightSpeed * dblBeta);
        
        return dblTime;
    }
     
    
    
    /*
     *  Testing and Debugging
     */
    
    
    /**
     *  Dump current state and content to output stream.
     *
     *  @param  os      output stream object
     */
    public void print(PrintWriter os)    {
        os.println("  Element - " + this.getId());
        os.println("  element type       : " + this.getType() );
        os.println("  element UID        : " + this.getUID() );
        os.println("  element length     : " + this.getLength() );
    };

};