package lam.android.tapatapp_api4.model;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import java.util.List;

import lam.android.tapatapp_api4.R;
import lam.android.tapatapp_api4.controller.activities.Activity_Main;

public class Child
{
    private void showlog (Object o) { Log.i("--" + this.getClass().getSimpleName(), o.toString()); }

    public Child() {
    }

    // ---------------------------------------------------------------------------------- Attributes

    public final static int TREATMENT_HOURS = 1;
    public final static int TREATMENT_PERCENTAGE = 2;

    public final static int ROL_ADMIN = 1;
    public final static int ROL_CARETAKER = 2;

    private int id;
    private int rol_id; // admin=1, caretaker=2
    public String name;
    public int averageAwakeADay;
    public int treatment_id; // 1 = hours / 2 = percentage
    public int horas_o_porcentaje;
    public boolean awake;
    public boolean wearingEyepatch;
    private int treatment_time_today;

    public int getTreatment_time_today() {
        return treatment_time_today;
    }

    public void setTreatment_time_today(int treatment_time_today) {
        this.treatment_time_today = treatment_time_today;
    }
// ------------------------------------------------------------------------------------- Getters

    public int              getId                 () { return id;                 }
    public int              getRol_id             () { return rol_id;             }

    public String           getName               () { return name;               }
    public int              averageAwakeADay      () { return averageAwakeADay;   }
    public int              getTreatment_id       () { return treatment_id;       }
    public int              getHoras_o_porcentaje () { return horas_o_porcentaje; }

    public boolean          isAwake               () { return awake;              }
    public boolean          isWearingEyepatch     () { return wearingEyepatch;    }

    // ------------------------------------------------------------------------------------- Setters

    public void setId                 (int id)                  { this.id = id;                                 }
    public void setRol_id             (int rol_id)              { this.rol_id = rol_id;                         }
    public void setAverageAwakeADay   (int averageAwakeADay)    { this.averageAwakeADay = averageAwakeADay;   }

    public void setName               (String name)             { this.name = name;                             }
    public void setTreatment_id       (int treatment_id)        { this.treatment_id = treatment_id;             }
    public void setHoras_o_porcentaje (int horas_o_porcentaje)  { this.horas_o_porcentaje = horas_o_porcentaje; }

    public void setAwake              (boolean awake)           { this.awake = awake;                           }
    public void setWearingEyepatch    (boolean wearingEyepatch) { this.wearingEyepatch = wearingEyepatch;       }

    // --------------------------------------------------------------------------------- Constructor

    public Child (int id)
    {
        this.id = id;
    }

    /**
     * Cuando obtenemos la ID de los niños y el rol con el usuario.
     * Login>MainActivity>ListOfChildren
     * @param id
     * @param rol_id
     */
    public Child (int id, int rol_id)
    {
        this.id = id;
        this.rol_id = rol_id;
    }

    public Child(int id,  String name, int rol_id, int averageAwakeADay, int treatment_id, int horas_o_porcentaje, int treatment_time_today) {
        this.id = id;
        this.name = name;
        this.rol_id = rol_id;
        this.averageAwakeADay = averageAwakeADay;
        this.treatment_id = treatment_id;
        this.horas_o_porcentaje = horas_o_porcentaje;
        this.treatment_time_today = treatment_time_today;
    }

    public Child(String name, int rol_id, int averageAwakeADay, int treatment_id, int horas_o_porcentaje) {
        this.name = name;
        this.rol_id = rol_id;
        this.averageAwakeADay = averageAwakeADay;
        this.treatment_id = treatment_id;
        this.horas_o_porcentaje = horas_o_porcentaje;
    }

    public Child (String name, int averageHoursAwakeADay, int treatment_id, int horas_o_porcentaje) // Constructor used in Activity_ChildCreate
    {
        this.name = name;
        this.treatment_id = treatment_id;
        this.averageAwakeADay = averageHoursAwakeADay;
        this.horas_o_porcentaje = horas_o_porcentaje;

        //setAverageTime(averageHoursAwakeADay); TODO <------------------------------------------------ aunque esto no se calcularia en el constructor, sino en el CreateChild Activity

    }

    public Child (int id, String name, int averageAwakeADay, int treatment_id, int horas_o_porcentaje) // Constructor used in DaoChild when retrieving data from the database server
    {
        this.id = id;
        this.name = name;
        this.averageAwakeADay = averageAwakeADay;
        this.treatment_id = treatment_id;
        this.horas_o_porcentaje = horas_o_porcentaje;

        //setAverageTime(averageAwakeADay);


    }

    public Child (Child copy) // Constructor used in ¿¿¿???
    {
        this.id = copy.getId();
        this.name = copy.getName();
        this.treatment_id = copy.getTreatment_id();
        this.horas_o_porcentaje = copy.getHoras_o_porcentaje();
        //this.taps = copy.getTaps();
        this.awake = copy.isAwake();
        this.wearingEyepatch = copy.isWearingEyepatch();
    }

    // ----------------------------------------------------------------------------------- To String

    @Override
    public String toString() {
        return "Child{" +
                "id=" + id +
                ", rol_id=" + rol_id +
                ", name='" + name + '\'' +
                ", averageAwakeADay=" + averageAwakeADay +
                ", treatment_id=" + treatment_id +
                ", horas_o_porcentaje=" + horas_o_porcentaje +
                ", awake=" + awake +
                ", wearingEyepatch=" + wearingEyepatch +
                ", treatment_time_today=" + treatment_time_today +
                '}';
    }


    // CALCULOS CHUNGUISIMOS

    /**
     * GET TIME LEFT TO STOP WEARING EYEPATCH
     * --------------------------------------
     * A este metodo lo llama el Activity_Main para mostrar el tiempo que falta para poder quitarle
     * el parche al niño.
     *
     * El Activity_Main sólo llama a este metodo si el tipo de contador del niño es porcentaje (false) y
     * si esta llevando el parche en este momento. Si estas dos condiciones no se cumplen, el Activity_Main
     * mostrará tres guiones --- en lugar de un numero. (O quizas muestre otra cosa ? No se)
     *
     * Lo que hace este metodo para calcular el tiempo que falta para quitarle el parche al niño es:
     * 1 - Comprobar el percentage de este objeto (getPercentage()).                                   Ejemplo: Tiene que llevar el parche durante el 30% del tiempo que está despierto al dia.
     * 2 - Comprobar el tiempo que el niño está despierto (getaverageAwakeADay()).                     Ejemplo: Suele estar despierto más o menos 14 horas al dia.
     * 3 - Hacer el %.                                                                                 Ejemplo: El 30% de 14 son 4,2.
     * 4 - Comprobar cuanto tiempo ha estado llevando el parche el niño hoy (getTodaysEyepatchSeconds()). Ejemplo: Hoy ha estado llevando el parche durante 2 horas.
     * 5 - Hacer la resta.                                                                             Ejemplo: 4,2 - 2 = 2,2   --->    Le quedan 2 horas más de parche hoy.
     *
     * (( importa lo que haya dormido hoy o las horas que faltan para que termine el dia? ))
     * @return
     */
    /*public String minutos_restantes_para_quitarse_el_parche ()
    {
        // para que este metodo tenga sentido, su tipo de contador debe ser por porcentaje y tiene que estar llevando el parche.
        // if (!treatment_id && isWearingEyepatch)



        // debe llevar el parche el 30% del tiempo que esta despierto ---------------\___ el 30% de 14 son 4,2
        // suele estar despierto 14 horas al dia (getaverageAwakeADay()) ------------/

        // hoy ha tenido el parche puesto 2 horas -----> 4,2 - 2 = 2,2 -------> le faltan 2,2 horas más de parche





        // si el niño no esta llevando el parche en este momento, este metodo no tiene sentido y devuelve guiones simbolizando "nada".
        if (!isWearingEyepatch()) return "---";

        long seconds = 0;

        /* si el tipo de contador es "horas" (treatment_id es true) (debe llevar el parche X horas al
        dia), simplemente resta el tiempo que ha estado llevando el parche hoy a las horas totales
        que debe llevarlo (convertidas a segundos), para encontrar la diferencia (lo que falta).*/
        /*if (treatment_id == 1) seconds = horas_o_porcentaje*60*60 - getTodaysEyepatchSeconds();

            // si, por el contrario, el tipo de contador es "porcentaje" (treatment_id es false) (debe llevar el parche durante el X% del tiempo que esté despierto durante el dia),
            // entonces lo que hace es calcular qué tanto por ciento total debe llevar el parche durante el dia (X * 100 / averageAwakeADay)
            // y luego a eso le resta el tiempo que ya ha estado llevando el parche para encontrar la diferencia (el tiempo que le falta para haber llevado el parche las horas necesarias)
        else seconds = (getHoras_o_porcentaje() * 100 / averageAwakeADay) - getTodaysEyepatchSeconds();

        // por ultimo, si lo que ha devuelto es menor o igual a 0, entonces devuelve tres guiones simbolizando que no queda nada.
        // (que seconds sea igual o menor que 0 significa que ya ha estado las horas necesarias para llevar el parche y por tanto la resta ha dado 0 o menos).
        if (seconds <= 0) return "---";

            // si no, entonces devuelve la duracion calculada (seconds son segundos). Y luego la Activity que llame a este metodo ya
        else return String.valueOf(seconds/60);
    }*/

    /*private long getTodaysEyepatchSeconds()
    {
        long secondsWearingEyepatch = 0;

        for (int i = taps.size(); i >= 0; i--) // empieza desde el final de la lista (desde los taps mas recientes)
        {
            Tap tap = taps.get(i);
            if (tap.getInit_date().getMonth() == MyTimeStamp.now().getMonth()) // si el tap es de hoy,
            {
                if (tap.getStatus_id() == 1) // si se trata de un "awake", entonces

                    // añade la duracion de ese tap al total de segundos que ha estado llevando el eyepatch
                    secondsWearingEyepatch += tap.getEnd_date().getSecond() - tap.getInit_date().getSecond();
            }

            // si llega a un tap que no es de hoy, para de contar.
            else break;
        }

        return secondsWearingEyepatch;
    }*/


    /**
     *
     */
    private void setaverageAwakeADay ()
    {
        // si la lista de taps esta vacia ... bueno. Eso seria un error, ya que en el constructor de child ya le añade taps basados en el numero dado por el usuario al crear el child.
        // la unica manera de que eso ocurra seria que el usuario haya borrado todos los taps manualmente (en el historial), o que haga mucho tiempo que no inicia la aplicacion y los taps se van borrando con el tiempo.
        // en ese caso, no haria nada. Dejaría el averageAwakeADay como está.


        // recorre la lista de taps desde el ultimo hacia atrás (del mas reciente hacia el más antiguo) y los va guardando en otra lista llamada last30days.
        // para de recorrer la lista cuando se topa con un tap cuya fecha de inicio sea de hace 30 dias o más, o cuando no encuentre más taps.

        // calcula cuantos dias representan los taps de last30days (podrian ser menos de 30 dias) (como calcularlo exactamente?).
        // lo transforma a segundos (segundosContados = diasContados * 24 * 60 * 60) y lo guarda en "segundosTotales"

        // Por cada tap en last30days calcula la duracion del tap en segundos (endDate - initDate) y la suma a "segundosDespierto".

        // ahora tenemos segundosTotales y segundosDespierto de los ultimos dias. A partir de aqui podemos sacar el % del tiempo que ha estado despierto los ultimos dias.
        // aplicamos ese mismo % a un solo dia (24 horas) para saber qué % del dia esta despierto. Ya tenemos la media.
    }

    /**
     * MODIFY AVERAGE TIME
     * -------------------
     * La variable global List<Tap> averageAwakeADay[] guarda los Tap
     * Estos taps se utilizan para calcular la aproximación de la cantidad de minutos que el niño está despierto cada dia (la media de minutos despierto al dia).
     * Cada dia a las 00:00 horas, el tap más antiguo de los 100 se elimina para dejar sitiola clase ??? llama a este metodo para que los nuevos datos afecten a esa variable y cambien la aproximación un poco.
     *
     * Lo que hace este metodo es: Coge la lista de taps que tiene (que son todos los que han sido añadidos o modificados hoy),
     * y por cada tap
     *
     * @param todaysAwakeTaps : Todos los taps de tipo "awake" cuya "date_modified" sea hoy.
     */
    private void modifyAverageTime (List<Tap> todaysAwakeTaps)
    {

        //cantidad de dias a cambiar:        1 dia  (24 horas)  (1440 minutos)





        /*
        ejemplo:
        si averageAwakeADay son 14 horas y los datos nuevos son:
              - los de hoy (ha estado despierto 16 horas)
              - y también ha añadido un tap para el dia anterior (calcula el totalTimeAwake del dia anterior con los nuevos datos y da que estuvo despierto 17 horas)
        entonces:

        averageAwakeADay:                          14 horas   ( 84O minutos)
        dia 1: horas despierto anteriores:         null       (   O minutos)
        dia 1: horas despierto nuevas:             16 horas   ( 96O minutos)
        dia 2: horas despierto anteriores:         13 horas   ( 78O minutos)
        dia 2: horas despierto nuevas:             17 horas   (1O2O minutos)

        imaginandome que averageAwakeADay se supone que esta calculado basado en 100 dias, estos 2 dias cambiarían tan solo un 2% del calculo final. por tanto:

        98 dias indican que duerme 14 horas (  84O minutos ) al dia.
        1 dia   indica  que duerme 16 horas (  96O minutos ) al dia.
        1 dia   indica  que duerme 17 horas ( 1O2O minutos ) al dia.



   Total de horas que tienen 100 dias: 2400        1405 son el total de horas que esta despierto durante 100 dias
                                        ||          ||
                                        \/          \/
                                    de 2400 horas, 1405 son el 59%
                                                               ||
                                                               \/
                           Tanto por ciento que duerme al dia: 59%    1440 son el total de minutos que tiene un dia
                                                               ||      ||
                                                               \/      \/
                                                            El 59% de 1440 minutos es 850 minutos
                                                                                       |
                                                                                       v
                        RESULTADO FINAL DE LA NUEVA MEDIA:     Duerme aproximadamente 850 minutos cada dia (14 horas y 10 segundos ---> Los segundos no se guardan por tanto la media no cambia, se queda en 14).










        imaginandome que averageAwakeADay se supone que esta calculado basado en 30 dias:

        28 dias indican que duerme 14 horas (  84O minutos ) al dia.
        1 dia   indica  que duerme 16 horas (  96O minutos ) al dia.
        1 dia   indica  que duerme 17 horas ( 1O2O minutos ) al dia.

   Total de minutos que tienen 30 dias: 43200        25500 son el total de minutos que esta despierto durante 30 dias
                                         ||           ||
                                         \/           \/
                                     de 43200 horas, 25500 son el 59%
                                                                  ||
                                                                  \/
                              Tanto por ciento que duerme al dia: 59%    1440 son el total de minutos que tiene un dia
                                                                  ||      ||
                                                                  \/      \/
                                                               El 59% de 1440 minutos es 850 minutos
                                                                                          |
                                                                                          v
                           RESULTADO FINAL DE LA NUEVA MEDIA:     Duerme aproximadamente 850 minutos cada dia (14 horas y 10 segundos ---> Los segundos no se guardan por tanto la media no cambia, se queda en 14).





        */
    }

    // Todo no estamos guardando en ningún lado el TIEMPO TOTAL que le queda al niño para llevar el aprche, entonces:
    //Lo calcula la app y lo guardamos aquí, o lo calcula la app pero lo guardamos en la bbdd;
    //Debería haber un contador hacía atrás,no?

    //TODO esto coge la media, no sé muy bien como llevas el orden...
    public int getaverageAwakeADay() {
        return averageAwakeADay;
    }

    public void setaverageAwakeADay(int averageAwakeADay) {
        this.averageAwakeADay = averageAwakeADay;
    }

    //TODO no se muy bien que es esto
    /*private int getaverageAwakeADay ()
    {
        // no hace falta que sepa cuantos taps ha comparado desde los inicios de los tiempos.
        // simplemente que esto afecte al numero fijo guardado, un poquito. Como un 10% quizas. (TODO crear el numero guardado)

        // TODO
        Tap ht1 = null;
        Tap ht2 = null; //new Tap("", LocalDateTime.now(), LocalDateTime.now());

        return Duration.between(ht1.getInit_date(), ht2.getEnd_date()).toMinutes();
    }*/
}
