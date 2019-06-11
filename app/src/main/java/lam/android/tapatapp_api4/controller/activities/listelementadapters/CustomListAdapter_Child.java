package lam.android.tapatapp_api4.controller.activities.listelementadapters;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.List;

import lam.android.tapatapp_api4.R;
import lam.android.tapatapp_api4.model.Child;

/**
 * Esta clase es utilizada por Activity_ListOfChildren y por Activity_ListOfTaps para cargar cada
 * elemento de la lista.
 */
public class CustomListAdapter_Child implements ListAdapter
{
    private void showlog (Object o) { Log.i("--" + this.getClass().getSimpleName(), o.toString()); }

    // ---------------------------------------------------------------------------- Global Variables

    private List<Child> list;
    private Context context;

    // -------------------------------------------------------------------------------- Constructors

    public CustomListAdapter_Child(Context context, List<Child> arrayList)
    {
        this.list = arrayList;
        this.context = context;
    }

    // ------------------------------------------------------------------------------------- Methods

    /**
     * Este metodo getView() lo que hace es generar el View de un elemento de la lista. No estoy
     * segura pero deduzco que a este metodo lo llama la clase ListAdapter cada vez que carga
     * un elemento de la lista.
     *
     * Lo que hace es crear un LayoutInflater con el context de la Activity_ListOfChildren (que le
     * pasó por el constructor al crear este objeto) y "inflar" el elemento (crear su componente
     * en el layout). Luego, inicializa ese componente en un objeto (TextView) para poder hacerle
     * un setText y mostrar el nombre del Child en cuestión.
     *
     * @param position : la posicion del elemento en la lista.
     * @param convertView : variable global "View" invisible porque forma parte de ListAdapter, que
     *                    es la clase a la que esta clase está extendiendo.
     * @param parent : no se
     * @return la vista generada por el metodo.
     */
    @Override public View getView (int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.list_element_child, null);

            TextView childButton = convertView.findViewById(R.id.textView_tapTypeInit);
            childButton.
                    setText(
                            list.
                                    get(
                                            position)
                                    .getName());
        }
        return convertView;
    }

    // Todos estos metodos de abajo venian asi por defecto. No se que hacen.

    @Override public boolean areAllItemsEnabled () { return false; }
    @Override public boolean isEnabled (int position) { return true; }
    @Override public void registerDataSetObserver (DataSetObserver observer) {}
    @Override public void unregisterDataSetObserver (DataSetObserver observer) {}
    @Override public int getCount () { return list.size(); }
    @Override public Object getItem (int position) { return position; }
    @Override public long getItemId (int position) { return position; }
    @Override public boolean hasStableIds () { return false; }
    @Override public int getItemViewType (int position) { return position; }
    @Override public int getViewTypeCount () { return list.size(); }
    @Override public boolean isEmpty () { return false; }
}
