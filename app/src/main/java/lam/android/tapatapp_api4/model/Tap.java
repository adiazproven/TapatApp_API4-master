package lam.android.tapatapp_api4.model;

import android.util.Log;

public class Tap
{
    private void showlog (Object o) { Log.i("--" + this.getClass().getSimpleName(), o.toString()); }

    public static final int AWAKE = 1;
    public static final int EYEPATCH = 2;
    // ---------------------------------------------------------------------------------- Attributes
    private int id;


    /**
     * State that the tap is referring to.
     * Can be "awake" or "wearing eyepatch".
     * When the child is asleep, there is no "awake" history during that time.
     * When the child is not wearing the eyepatch, there is no "Wearing eyepatch" history during that time.
     */
    private int status_id; // 1 = awake, 2 = eyepatch

    /**
     * Time in which the status_id starts.
     * It also represents the time in which its opposite status_id would end. For example, if this is
     * "awake", this will also be the time in which the "asleep" would end, if it were saved in
     * the system.
     */
    private MyTimeStamp init_date;

    /**
     * Time in which the status_id ends.
     * It also represents the time in which its opposite status_id would start. For example, if this is
     * "wearing eyepatch", this will also be the time in which the "not wearing eyepatch" would
     * start, if it were saved in the system.
     */
    private MyTimeStamp end_date;

    private int child_id;

    // -------------------------------------------------------------------------------- Constructors

    //public Tap(int id, child_id, status_id, dateInit, initStatus, dateEnd, endStatus);

    //public Tap (int id, status_id, initDate, endDate);

    public Tap (/*int id,*/ int status_id, MyTimeStamp init_date, MyTimeStamp end_date)
    {
        //this.id = id;
        setStatus_id(status_id);
        setInit_date(init_date);
        setEnd_date(end_date);
    }

    public Tap (int id, int child_id, int status_id, MyTimeStamp init_date, MyTimeStamp end_date)
    {
        this.id = id;
        this.child_id = child_id;
        this.status_id = status_id;
        this.init_date = init_date;
        this.end_date = end_date;
    }

    public Tap (int id, int child_id, int status_id, MyTimeStamp end_date)
    {
        this.id = id;
        this.child_id = child_id;
        this.status_id = status_id;
        this.end_date = end_date;
    }


    public Tap ( int child_id, int status_id, MyTimeStamp init_date, MyTimeStamp end_date)
    {
        this.child_id = child_id;
        setStatus_id(status_id);
        setInit_date(init_date);
        this.end_date = end_date;
    }


    public Tap (int id, MyTimeStamp end_date)
    {
        this.id = id;
        setEnd_date(end_date);
    }

    public Tap (int id){
        this.id = id;
    }

    public Tap (Tap tap)
    {
        this.id = tap.getId();
        this.status_id = tap.getStatus_id();
        this.init_date = tap.getInit_date();
        this.end_date = tap.getEnd_date();
        this.child_id = tap.getChild_id();
    }

    // ----------------------------------------------------------------------------------- Accessors

    public int getChild_id () { return child_id; }
    public void setChild_id ( int child_id ) { this.child_id = child_id; }

    public int getId () { return id; }

    public int getStatus_id() { return status_id; }
    public void setStatus_id (int status_id) { this.status_id = status_id; }

    public MyTimeStamp getInit_date () { return init_date; }
    public void setInit_date (MyTimeStamp init_date)
    {
        /* TODO comprobar que no superponga otro tap del mismo status_id.
        Ejemplo: Un "awake" empieza a las 12 mientras que el anterior termina a las 13, una hora después. */
        this.init_date = init_date;
    }

    public MyTimeStamp getEnd_date () { return end_date; }
    public void setEnd_date (MyTimeStamp end_date)
    {
        /* TODO comprobar que no superponga otro tap del mismo status_id.
        Ejemplo: Un "awake" termina a las 13 mientras que el anterior termina a las 12, una hora antes. */
        this.end_date = end_date;
    }

    @Override
    public String toString() {
        return "Tap{" +
                "id=" + id +
                ", status_id=" + status_id +
                ", init_date=" + init_date +
                ", end_date=" + end_date +
                ", child_id=" + child_id +
                '}';
    }
}
