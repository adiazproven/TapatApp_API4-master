package lam.android.tapatapp_api4.controller.activities;

import android.app.Activity;

import java.util.ArrayList;
//TODO ESTO QUE ES?????????????????????????????????????????????????????????????
public class MyActivityManager
{
    private ArrayList<String> openedActivityList = new ArrayList<>();

    public boolean activityIsAlreadyOpened (Class activity)
    {
        String activityName = activity.getSimpleName();
        if (!openedActivityList.contains(activityName))
        {
            openedActivityList.add(activityName);
            return true; // si retorna true, que haya     intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);     antes de startActivity
        }
        else return false;
    }

    public void finishActivity (Activity activity)
    {
        String activityName = activity.getClass().getSimpleName();
        if (openedActivityList.contains(activityName)) openedActivityList.remove(activityName);
    }
}
