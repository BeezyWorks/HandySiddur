package com.mattaniahbeezy.icecreamlibrary;

import android.content.Context;
import android.location.Location;

import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.util.GeoLocation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Class for getting a ComplexZmanimCalculator based on your location. Contains several helpful methods and the Zman class.
 */
public class ZmanGetter extends ComplexZmanimCalendar {
    public static final int GPS_LOCATION = 0;
    public static final int SAVED_LOCATION = 2;
    private Context context;

    final static Double DEFAULT_LONGITUDE = 35.235806;
    final static Double DEFAULT_LATIUDE = 31.777972;
    final static Double DEFAULT_ELEVATION = 0d;

    /**
     * Creates a new instance of a ZmanGetter with todays date. It attempts to get a location, if none is available it pulls the information from SharedPreferences to make a GeoLocation
     *
     * @param context
     * @return
     */
    public static ZmanGetter getCZC(Context context) {
        return ZmanGetter.getCZC(context, Calendar.getInstance(), null);
    }

    public static ZmanGetter getCZC(Context context, Calendar date) {
        return ZmanGetter.getCZC(context, date, null);
    }

    public static ZmanGetter getCZC(Context context, Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return ZmanGetter.getCZC(context, cal, null);
    }

    public static ZmanGetter getCZC(Context context, Calendar date, Location location) {
        ZmanGetter czc = new ZmanGetter(context);
        czc.setGeoLocation(getGeoLocation(location, context));
        czc.setCandleLightingOffset(new SettingsHelper(context).getInt(SettingsHelper.candelLightKey, 18));
        czc.setCalendar(date);
        return czc;
    }

    /**
     * Don't use this constructor. Use the static methods getCZC instead
     *
     * @param context
     */
    public ZmanGetter(Context context) {
        this.context = context;
    }

    public boolean isAfterSunset() {
        return new DaveningInfo(context).getGregorianCalendar().getTimeInMillis() > getSunset().getTime();
    }

    public boolean isAfterChatzos() {
        return new DaveningInfo(context).getGregorianCalendar().getTimeInMillis() > getChatzos().getTime();
    }

    public boolean isAfter(Date zman) {
        if (zman == null)
            return false;
        return new DaveningInfo(context).getGregorianCalendar().getTimeInMillis() > zman.getTime();
    }

    public DAILY_ZMANIM getApproachingZman() {
        List<DAILY_ZMANIM> zmanimList = getFullLuach();
        int nextZmanIndex = 0;
        while (isAfter(zmanimList.get(nextZmanIndex).getDate(this)) && nextZmanIndex < zmanimList.toArray().length - 1)
            nextZmanIndex++;
        return zmanimList.get(nextZmanIndex);
    }

    public static GeoLocation getGeoLocation(Location location, Context context) {
        if (location != null) {
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            double elevation = location.getAltitude();
            if (elevation <= 0d)
                elevation = 1d;
            return new GeoLocation("geo", latitude, longitude, elevation, TimeZone.getDefault());
        }

        SettingsHelper settingsHelper = new SettingsHelper(context);
        try {
            double longitude = Double.valueOf(settingsHelper.getString(SettingsHelper.longitudeKey, String.valueOf(DEFAULT_LONGITUDE)));
            double latitude = Double.valueOf(settingsHelper.getString(SettingsHelper.latitudeKey, String.valueOf(DEFAULT_LATIUDE)));
            double elevation = Double.valueOf(settingsHelper.getString(SettingsHelper.elevationKey, String.valueOf(DEFAULT_ELEVATION)));
            if (elevation <= 0d)
                elevation = 1d;
            return new GeoLocation("geo", latitude, longitude, elevation, TimeZone.getDefault());
        } catch (ClassCastException e) {
            return new GeoLocation("geo", DEFAULT_LATIUDE, DEFAULT_LONGITUDE, DEFAULT_ELEVATION, TimeZone.getDefault());
        }

    }

    public List<DAILY_ZMANIM> getFullLuach() {
        DaveningInfo daveningInfo = new DaveningInfo(context);
        daveningInfo.setDate(getCalendar());

        List<DAILY_ZMANIM> zmanimList = new ArrayList<>();
        for (DAILY_ZMANIM daily_zmanim : DAILY_ZMANIM.values()) {
            if (daily_zmanim == DAILY_ZMANIM.SIUM_HATZOM) {
                if (daveningInfo.isTaanis())
                    zmanimList.add(daily_zmanim);
                continue;
            }
            if (daily_zmanim == DAILY_ZMANIM.HADLAKAS_NEIROS) {
                if (daveningInfo.isCandelLight())
                    zmanimList.add(daily_zmanim);
                continue;
            }
            if ((daily_zmanim == DAILY_ZMANIM.BIUR_CHAMETZ || daily_zmanim == DAILY_ZMANIM.ACHILAS_CHAMETZ)) {
                if (daveningInfo.getYomTovIndex() == JewishCalendar.EREV_PESACH)
                    zmanimList.add(daily_zmanim);
                continue;
            }
            if (daily_zmanim == DAILY_ZMANIM.TEFILA_GRA && !(daveningInfo.getYomTovIndex() == JewishCalendar.EREV_PESACH)) {
                zmanimList.add(daily_zmanim);
                continue;
            }

            if (zmanimList.indexOf(daily_zmanim) == -1)
                zmanimList.add(daily_zmanim);
        }

        return zmanimList;
    }

    public enum DAILY_ZMANIM {
        ALOS, MISHYAKIR, NEITZ, SHMA_MGA, SHMA_GRA, ACHILAS_CHAMETZ, BIUR_CHAMETZ, TEFILA_MGA, TEFILA_GRA, CHATZOS, MINCHA_GEDOLA, MINCHA_KETANA, PLAG_HAMINCHA, HADLAKAS_NEIROS, SHKIA, SIUM_HATZOM, TZEIS, TZEIS_RT,;

        public Date getDate(ZmanGetter czc) {
            switch (this) {
                case ALOS:
                    return czc.getAlosHashachar();
                case MISHYAKIR:
                    return czc.getMisheyakir11Point5Degrees();
                case NEITZ:
                    return czc.getSunrise();
                case SHMA_MGA:
                    return czc.getSofZmanShmaMGA();
                case SHMA_GRA:
                    return czc.getSofZmanShmaGRA();
                case TEFILA_MGA:
                    return czc.getSofZmanTfilaMGA();
                case TEFILA_GRA:
                    return czc.getSofZmanTfilaGRA();
                case CHATZOS:
                    return czc.getChatzos();
                case MINCHA_GEDOLA:
                    return czc.getMinchaGedola();
                case MINCHA_KETANA:
                    return czc.getMinchaKetana();
                case PLAG_HAMINCHA:
                    return czc.getPlagHamincha();
                case SHKIA:
                    return czc.getSunset();
                case TZEIS:
                    return czc.getTzais();
                case TZEIS_RT:
                    return czc.getTzais72Zmanis();
                case HADLAKAS_NEIROS:
                    return czc.getCandleLighting();
                case SIUM_HATZOM:
                    return czc.getTzaisGeonim7Point083Degrees();
                case ACHILAS_CHAMETZ:
                    return czc.getSofZmanAchilasChametzGRA();
                case BIUR_CHAMETZ:
                    return czc.getSofZmanBiurChametzGRA();
                default:
                    return czc.getAlosHashachar();
            }
        }

        public CharSequence getTimeString(CharSequence pattern, ZmanGetter czc) {
            SimpleDateFormat sFat = new SimpleDateFormat(pattern.toString());
            sFat.setTimeZone(TimeZone.getDefault());
            return sFat.format(getDate(czc));
        }

        public String getName() {
            switch (this) {
                case ALOS:
                    return "עלות השחר";
                case MISHYAKIR:
                    return "משיכיר";
                case NEITZ:
                    return "הנץ החמה";
                case SHMA_MGA:
                    return "שמע מג''א";
                case SHMA_GRA:
                    return "שמע גר''א";
                case TEFILA_MGA:
                    return "תפילה מג''א";
                case TEFILA_GRA:
                    return "תפילה גר''א";
                case CHATZOS:
                    return "חצות היום";
                case MINCHA_GEDOLA:
                    return "מנחה גדולה";
                case MINCHA_KETANA:
                    return "מנחה קטנה";
                case PLAG_HAMINCHA:
                    return "פלג המנחה";
                case SHKIA:
                    return "שקיעת החמה";
                case TZEIS:
                    return "צאת הככבים";
                case TZEIS_RT:
                    return "צאת ר''ת";
                case HADLAKAS_NEIROS:
                    return "הדלקת נרות";
                case SIUM_HATZOM:
                    return "סיום הצום";
                case ACHILAS_CHAMETZ:
                    return "ס''ז אכילת חמץ";
                case BIUR_CHAMETZ:
                    return "ס''ז ביור חמץ";
            }
            return null;
        }
    }

    public Date getYesterdaySunset() {
        ZmanGetter yesterday = (ZmanGetter) this.clone();
        Calendar yesterdayCal = Calendar.getInstance();
        yesterdayCal.roll(Calendar.DATE, -1);
        yesterday.setCalendar(yesterdayCal);
        return yesterday.getSeaLevelSunset();
    }
}