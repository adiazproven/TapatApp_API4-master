package lam.android.tapatapp_api4.model;

import android.util.Log;

import java.util.Calendar;

public class MyTimeStamp
{
    private void showlog (Object o) { Log.i("--" + this.getClass().getSimpleName(), o.toString()); }

    // ---------------------------------------------------------------------------------- Attributes

    private Calendar calendar;
    public Calendar getCalendar () { return calendar; }

    public static final int DATE = 1;
    public static final int TIME = 2;

    public void set (int type, int year_hour, int month_minute, int day_second)
    {
        if (type == DATE)
        {
            calendar.set(Calendar.YEAR, year_hour);
            calendar.set(Calendar.MONTH, month_minute);
            calendar.set(Calendar.DAY_OF_MONTH, day_second);
        }
        else
        {
            calendar.set(Calendar.HOUR_OF_DAY, year_hour);
            calendar.set(Calendar.MINUTE, month_minute);
            calendar.set(Calendar.SECOND, day_second);
        }
    }

    // -------------------------------------------------------------------------------- Constructors

    /**
     * Crear un objeto MyTimeStamp a partir de una serie de numeros enteros (año, mes, dia...)
     * Ejemplo:
     * MyTimeStamp mts = new MyTimeStamp(2019, 5, 22, 17, 2, 44);
     */
    public MyTimeStamp(int year, int month, int day, int hour, int minute, int second)
    {
        calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
    }

    /**
     * Crear un objecto MyTimeStamp a partir de un String con formato 2019-05-22T13:37:59
     * Ejemplo:
     * MyTimeStamp mts = new MyTimeStamp("2019-05-22T13:37:59");
     *
     * tambien funciona con el siguiente formato:
     *
     * Crear un objecto MyTimeStamp a partir de un String con formato {"date":{"year":2019,"month":4,"day":5},"time":{"hour":10,"minute":10,"second":0,"nano":0}}
     * Ejemplo:
     * MyTimeStamp mts = new MyTimeStamp("{"date":{"year":2019,"month":4,"day":5},"time":{"hour":10,"minute":10,"second":0,"nano":0}}");
     */
    public MyTimeStamp(String fecha)
    {
        try
        {
            setCalendarByStringWithTimestampFormat(fecha);
        }
        /* Si salta esta Exception, es posible que el problema sea que el String pasado en los
         * parentesis no tenga formato 2019-0-22T13:37:59 sino formato {"date":{"year":2019,"month":4,"day":5},"time":{"hour":10,"minute":10,"second":0,"nano":0}}
         * En ese caso, usamos el metodo replace_toString_format_for_timestamp_format()
         */
        catch (NumberFormatException nfe) // Si salta esta Exception, es posible que el problema sea que el String pasado en los parentesis no tenga formato
        {
            try
            {
                fecha = replace_toString_format_for_timestamp_format(fecha);
                setCalendarByStringWithTimestampFormat(fecha);
            }
            catch (NumberFormatException ex) // Si aqui tambien salta una Excepcion entonces ya no se.
            {
                showlog("Error creating MyTimeStamp: " + ex);
            }
        }
    }

    /**
     * Inicializa el Calendar de este objeto (que es lo que guarda la fecha bien bien) a partir de la String que le pasas por parentesis.
     * @param timestamp_date String que guarda una fecha con formato 2019-0-22T13:37:59
     */
    private void setCalendarByStringWithTimestampFormat (String timestamp_date) throws NumberFormatException
    {
        calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR,         Integer.valueOf(timestamp_date.substring(0,4)));
        calendar.set(Calendar.MONTH,        Integer.valueOf(timestamp_date.substring(5,7)));
        calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(timestamp_date.substring(8,10)));
        calendar.set(Calendar.HOUR_OF_DAY,  Integer.valueOf(timestamp_date.substring(11,13)));
        calendar.set(Calendar.MINUTE,       Integer.valueOf(timestamp_date.substring(14,16)));
        calendar.set(Calendar.SECOND,       Integer.valueOf(timestamp_date.substring(17,19)));
    }

    /**
     * @param json_date String que guarda una fecha con formato {"date":{"year":2019,"month":4,"day":5},"time":{"hour":10,"minute":10,"second":0,"nano":0}}
     * @return          String que guarda una fecha con formato 2019-0-22T13:37:59
     */
    private String replace_toString_format_for_timestamp_format (String json_date) throws NumberFormatException
    {
        json_date = json_date.replace("  {\"date\":{\"year\":   ","")
                .replace("  ,\"month\":            ","-")
                .replace("  ,\"day\":              ","-")
                .replace("  },\"time\":{\"hour\":  ","T")
                .replace("  ,\"minute\":           ",":")
                .replace("  ,\"second\":           ",":")
                .replace("  ,\"nano\":0}}          ","");

        return json_date;
    }

    /**
     * Crear un objeto MyTimeStamp a partir de un objeto Calendar.
     * Ejemplo:
     * MyTimeStamp mts = new MyTimeStamp(Calendar.getInstance());
     */
    public MyTimeStamp(Calendar calendar)
    {
        this.calendar = calendar;
    }

    /** Copy Constructor */
    public MyTimeStamp(MyTimeStamp original)
    {
        this.calendar = (Calendar)original.calendar.clone();
    }

    // ------------------------------------------------------------------------------------- Methods

    public static MyTimeStamp now ()
    {
        return new MyTimeStamp(Calendar.getInstance());
    }

    /** COMPARE TO ANOTHER MYTIMESTAMP
     * If it returns 0, this MyTimeStamp is equal to the other.
     * if it returns a positive number, this MyTimeStamp happens before the other.
     * if it returns a negative number, this MyTimeStamp happens after the other.
     */
    public int compareTo (MyTimeStamp other)
    {
        return calendar.compareTo(other.calendar);
    }

    /** returns seconds between this MyTimeStamp and other */
    public long minus (MyTimeStamp other)
    {
        MyTimeStamp mts = new MyTimeStamp(this);
        mts.calendar.add(Calendar.YEAR,         -other.getYear  ());
        mts.calendar.add(Calendar.MONTH,        -other.getMonth ());
        mts.calendar.add(Calendar.DAY_OF_MONTH, -other.getDay   ());
        mts.calendar.add(Calendar.HOUR_OF_DAY,  -other.getHour  ());
        mts.calendar.add(Calendar.MINUTE,       -other.getMinute());
        mts.calendar.add(Calendar.SECOND,       -other.getSecond());
        return mts.calendar.getTimeInMillis() / 1000;
    }

    /** returns this same MyTimeStamp but 30 days ago */
    public MyTimeStamp minusDays (int days)
    {
        MyTimeStamp mts = new MyTimeStamp(this);
        mts.calendar.add(Calendar.DAY_OF_MONTH, -days);
        return mts;
    }

    public MyTimeStamp plusSeconds (int seconds)
    {
        MyTimeStamp mts = new MyTimeStamp(this);
        mts.calendar.add(Calendar.SECOND, +seconds);
        showlog(mts.toString());
        return mts;
    }

    /**
     * Convertir a String con formato 2019-05-22T13:37:59
     * Ejemplo:
     * String tiempoFormateado = mts.format();
     * (en este ejemplo, mts es el nombre de un objeto MyTimeStamp)
     * (en ese ejemplo la fecha seria: año 2019, mes 5, dia 22, hora 13, minuto 37, segundo 59)
     */
    public String format ()
    {
        StringBuilder sb = new StringBuilder();

        sb
                .append(calendar.get(Calendar.YEAR))
                .append("-");
        if (calendar.get(Calendar.MONTH) < 10) sb.append("0");
        sb
                .append(calendar.get(Calendar.MONTH)+1)
                .append("-");
        if (calendar.get(Calendar.DAY_OF_MONTH) < 10) sb.append("0");
        sb
                .append(calendar.get(Calendar.DAY_OF_MONTH))
                .append("T");
        if (calendar.get(Calendar.HOUR_OF_DAY) < 10) sb.append("0");
        sb
                .append(calendar.get(Calendar.HOUR_OF_DAY))
                .append(":");
        if (calendar.get(Calendar.MINUTE) < 10) sb.append("0");
        sb
                .append(calendar.get(Calendar.MINUTE))
                .append(":");
        if (calendar.get(Calendar.SECOND) < 10) sb.append("0");
        sb
                .append(calendar.get(Calendar.SECOND));

        return sb.toString();
    }

    /**
     * Convertir a String con formato 2019-05-22T13:37:59
     * Ejemplo:
     * String tiempoFormateado = mts.format();
     * (en este ejemplo, mts es el nombre de un objeto MyTimeStamp)
     * (en ese ejemplo la fecha seria: año 2019, mes 5, dia 22, hora 13, minuto 37, segundo 59)
     */
    public String format2 ()
    {
        StringBuilder sb = new StringBuilder();

        sb
                .append(calendar.get(Calendar.YEAR))
                .append("-");
        if (calendar.get(Calendar.MONTH) < 10) sb.append("0");
        sb
                .append(calendar.get(Calendar.MONTH)+1)
                .append("-");
        if (calendar.get(Calendar.DAY_OF_MONTH) < 10) sb.append("0");
        sb
                .append(calendar.get(Calendar.DAY_OF_MONTH));

        return sb.toString();
    }

    public String format3 ()
    {
        StringBuilder sb = new StringBuilder();

        if (calendar.get(Calendar.HOUR_OF_DAY) < 10) sb.append("0");
        sb
                .append(calendar.get(Calendar.HOUR_OF_DAY))
                .append(":");
        if (calendar.get(Calendar.MINUTE) < 10) sb.append("0");
        sb
                .append(calendar.get(Calendar.MINUTE))
                .append(":");
        if (calendar.get(Calendar.SECOND) < 10) sb.append("0");
        sb
                .append(calendar.get(Calendar.SECOND));

        return sb.toString();
    }


    // -------------------------------------------------------------------------- Calendar Accessors

    public int getYear   () { return calendar.get(Calendar.YEAR        ); }
    public int getMonth  () { return calendar.get(Calendar.MONTH       ); }
    public int getDay    () { return calendar.get(Calendar.DAY_OF_MONTH); }
    public int getHour   () { return calendar.get(Calendar.HOUR_OF_DAY ); }
    public int getMinute () { return calendar.get(Calendar.MINUTE      ); }
    public int getSecond () { return calendar.get(Calendar.SECOND      ); }

    @Override public String toString ()
    {
        return format();
    }
}
