package com.mattaniahbeezy.icecreamlibrary;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class DaveningInfo extends JewishCalendar {
    public Context context;
    private SharedPreferences sharedPref;


    public DaveningInfo(Context context) {
        this.context = context;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPref.getBoolean("manualDate", false)) {
            int month = sharedPref.getInt("month", 1);
            Calendar now = Calendar.getInstance();
            now.set(sharedPref.getInt("year", 1990), month, sharedPref.getInt("day", 1));
            setDate(now);
        }
        setUseModernHolidays(sharedPref.getBoolean("modern", true));
        setInIsrael(sharedPref.getBoolean("inIsrael", false));
    }

    /**
     *Returns a List of JewishCalendar objects that repreresent a Yom Tov, Rosh Chodesh or has a Parsha associated with it.
     * @param jDate A JewishCalendar to begin calulating from
     * @param numberOfDays The number of events to add to the list
     * @return A List of JewishCalendar objects, each representing an event
     */

    public static List<JewishCalendar> getUpcomingEvents(JewishCalendar jDate, int numberOfDays){
        LinkedList<JewishCalendar> calendars = new LinkedList<JewishCalendar>();
        JewishCalendar calendarDate = (JewishCalendar) jDate.clone();
        while (calendars.toArray().length < numberOfDays) {
            calendarDate.forward();
            if (calendarDate.getYomTovIndex() > -1)
                calendars.add((JewishCalendar) calendarDate.clone());
            else if (calendarDate.isRoshChodesh())
                calendars.add((JewishCalendar) calendarDate.clone());
            else if (calendarDate.getParshaIndex() > -1)
                calendars.add((JewishCalendar) calendarDate.clone());
        }

        return calendars;
    }

    /**
     *
     * @return true for the first thirty days following the change in girsa for ברך עלינו
     */
    public boolean isRainNew() {
        if (!sharedPref.getBoolean("rainMinder", true))
            return false;
        if (getDayOfOmer() > -1 && getDayOfOmer() < 31)
            return true;

        Calendar rainStart = firstDayOfGoyRain();
        if (sharedPref.getBoolean("inIsrael", false))
            rainStart = firstDayOfIsraelRain();

        Calendar today = Calendar.getInstance();
        today.set(getGregorianYear(), getGregorianMonth(), getGregorianDayOfMonth());

        int rainDifference = today.get(Calendar.DAY_OF_YEAR) - rainStart.get(Calendar.DAY_OF_YEAR);
        if (rainDifference < 30 && rainDifference > 0)
            return true;
        return false;
    }

    /**
     *
     * @return true for the first thirty days following the girsa change in ברכת גבורות
     */

    public boolean isSummerNew() {
        if (!sharedPref.getBoolean("rainMinder", true))
            return false;
        if (getDayOfOmer() > -1 && getDayOfOmer() < 31)
            return true;
        if (getJewishMonth() == TISHREI && getJewishDayOfMonth() > 22)
            return true;
        if (getJewishMonth() == KISLEV && getJewishDayOfMonth() < 23)
            return true;


        return false;
    }

    private int dateOfGoyRain() {
        int goyRainDate = 4;
        if (new ZmanGetter(context).isAfterSunset()) {
            goyRainDate = 3;
        }
        if (LeapYear(getGregorianYear() + 1)) {
            goyRainDate++;
        }
        return goyRainDate;
    }

    private Calendar firstDayOfGoyRain() {
        Calendar cal = Calendar.getInstance();
        cal.set(getGregorianYear(), Calendar.DECEMBER, dateOfGoyRain());
        return cal;
    }

    private Calendar firstDayOfIsraelRain() {
        Calendar cal = Calendar.getInstance();
        JewishCalendar rainDate = new JewishCalendar();
        rainDate.setJewishDate(getJewishYear(), 8, 6);
        cal.set(rainDate.getGregorianYear(), rainDate.getGregorianMonth(), rainDate.getGregorianDayOfMonth());
        return cal;
    }

    /**
     *
     * @return true if there is a Yom Tov within the coming week (should Vayehi Noam be omitted)
     */

    public boolean upcomingYomTov() {
        JewishCalendar yomTov = (JewishCalendar) clone();
        for (int i = 0; i < 6; i++) {
            yomTov.forward();
            int index = yomTov.getYomTovIndex();
            if (index == JewishCalendar.PESACH || index == JewishCalendar.SHAVUOS || index == JewishCalendar.ROSH_HASHANA || index == JewishCalendar.YOM_KIPPUR || index == JewishCalendar.SUCCOS || index == JewishCalendar.SHEMINI_ATZERES || index == JewishCalendar.SIMCHAS_TORAH)
                return true;
        }
        return false;
    }

    /**
     *
     * @return true if ברכת לבנה may be said
     */
    public boolean isBirkasLevana() {
        JewishCalendar jTomorrow = getjTomorrow();
        Date kidushMoonEnd = jTomorrow.getSofZmanKidushLevana15Days();
        Date kidushMoonStart = jTomorrow.getTchilasZmanKidushLevana3Days();
        Date kidushMoonStartLate = jTomorrow.getTchilasZmanKidushLevana7Days();

        Calendar cal = Calendar.getInstance();
        cal.set(getGregorianYear(), getGregorianMonth(), getGregorianDayOfMonth());

        Date kiddushLevanaDate = cal.getTime();

        boolean kidushLevana = kiddushLevanaDate.compareTo(kidushMoonStart) > 0 && kiddushLevanaDate.compareTo(kidushMoonEnd) < 0;
        boolean kidushLevanaLate = kiddushLevanaDate.compareTo(kidushMoonStartLate) > 0 && kiddushLevanaDate.compareTo(kidushMoonEnd) < 0;
        boolean lateMoon = sharedPref.getBoolean("lateMoon", false);
        return ((kidushLevana && lateMoon == false) || (kidushLevanaLate && lateMoon));
    }

    /**
     *
     * @return true if for Mondays Thursdays and fastdays
     */
    public boolean isKria() {
        int dayOfWeek = getDayOfWeek();
        if (dayOfWeek == 2 || dayOfWeek == 5)
            return true;
        if (isTaanis())
            return true;
        return false;
    }

    /**
     *
     * @return true if Avinu Malkeinu is said. Goes by Ashkenazi custom (includes regular fast days).
     */
    public boolean isAvinu() {
        if (isEseresYemeiTeshuva())
            return true;
        if (getYomTovIndex() == EREV_YOM_KIPPUR && getDayOfWeek() == 6)
            return true;
        if (isTaanis())
            return true;

        return false;
    }

    /**
     *
     * @return true between Rosh Chodesh Elul and Shmini Atzeres
     */
    public boolean isLdovid() {
        if (getJewishMonth() == 6)
            return true;
        if (getJewishMonth() == 7 && getJewishDayOfMonth() < 22)
            return true;
        return false;
    }

    /**
     *
     * @return true if it's Purim. Takes Yerushaleim and leap years into account
     */
    public boolean isPurim() {
        if (sharedPref.getBoolean("inJerusalem", false) && getYomTovIndex() == SHUSHAN_PURIM)
            return true;
        if (!sharedPref.getBoolean("inJerusalem", false) && getYomTovIndex() == PURIM)
            return true;
        return false;
    }

    /**
     *
     * @return a JewishCalendar object that is one day forward
     */
    public JewishCalendar getjTomorrow() {
        JewishCalendar date = (JewishCalendar) clone();
        date.forward();
        return date;
    }

    public boolean isErevPurim() {
        if (sharedPref.getBoolean("inJerusalem", false) && getYomTovIndex() == PURIM)
            return true;

        if (!sharedPref.getBoolean("inJerusalem", false) && getjTomorrow().getYomTovIndex() == PURIM)
            return true;
        return false;
    }

    public boolean isLeilBedika() {
        return (getJewishDayOfMonth() == 13 && getJewishMonth() == 1);
    }

    public boolean isRain(){
        boolean inIsrael=sharedPref.getBoolean("inIsrael", false);
        if (getGregorianMonth() >= 0 && getJewishMonth() > 9)
            return true;
        if (getJewishMonth()==NISSAN&&getJewishDayOfMonth()<15)
            return true;
        if (getJewishMonth() > KISLEV && inIsrael)
            return true;
        if(getJewishMonth()==KISLEV && getJewishDayOfMonth()==8&&inIsrael)
            return true;
        if (isGoyRain())
            return true;

        return false;
    }

    private boolean isGoyRain(){
        int goyRainDate = 4;
        if (new ZmanGetter(context).isAfterSunset()) {
            goyRainDate = 3;
        }
        if (LeapYear(getGregorianYear() + 1)) {
            goyRainDate++;
        }

        if (getGregorianDayOfMonth() > goyRainDate && getGregorianMonth() == 11)
            return true;

        return false;
    }

    public boolean isSummer(){
        if (getJewishMonth() > 0 && getJewishMonth() < 8) {
            if (getJewishMonth() == TISHREI && getJewishMonth() > 22) {
                return false;
            } else if (getJewishMonth() == NISSAN && getJewishDayOfMonth() < 15) {
                return false;
            } else {
                return true;
            }
        }

        return false;
    }

    public boolean isErevNoTachanun(){
        if (getDayOfWeek()==6)
            return true;
        if (getJewishMonth()==NISSAN)
            return true;
        if (getjTomorrow().getYomTovIndex()==TU_BESHVAT||getjTomorrow().getYomTovIndex()==TU_BEAV)
            return true;

        return (isErevChanuka() || isErevRoshChodesh() || isErevPurim() || getDayOfOmer() == 32  || isErevTisha());
    }

    public boolean isNoTachanun() {
        if (sharedPref.getBoolean("avel", false))
            return true;
        if (getJewishMonth() == NISSAN)
            return true;
        return (isHatzmaus() || isYomYerushaleim() || isPurim() || isHallel() || isNoTachanunAfterYomKippur() || getYomTovIndex() == JewishCalendar.EREV_ROSH_HASHANA || getYomTovIndex() == JewishCalendar.TU_BEAV || getYomTovIndex() == JewishCalendar.TU_BESHVAT || getYomTovIndex() == JewishCalendar.PURIM_KATAN || getDayOfOmer() == 33 || getYomTovIndex() == JewishCalendar.PESACH_SHENI || isNoTachanunAfterShavuos() || isTisha());
    }

    public boolean isNoTachanunAfterShavuos() {
        if (getJewishMonth() == SIVAN && getJewishDayOfMonth() < 13)
            return true;
        return false;
    }

    public boolean isNoTachanunAfterYomKippur() {
        if (getJewishMonth() == TISHREI && getJewishDayOfMonth() > 8)
            return true;
        return false;
    }

    public boolean isConductor() {
        return !(isHatzmaus() || isYomYerushaleim() || sharedPref.getBoolean("avel", false) || isPesach() || isTisha() || isChanukah() || isPurim() || isRoshChodesh() || isSuccos() || (getJewishMonth() == 1 && getJewishDayOfMonth() == 14) || (getJewishMonth() == 7 && getJewishDayOfMonth() == 9));
    }

    public boolean isMotzach() {
        int yomTovIndex = getYomTovIndex();
        return yomTovIndex == JewishCalendar.PESACH || yomTovIndex == JewishCalendar.SHAVUOS || yomTovIndex == JewishCalendar.ROSH_HASHANA || yomTovIndex == JewishCalendar.YOM_KIPPUR || yomTovIndex == JewishCalendar.SUCCOS || yomTovIndex == JewishCalendar.SHEMINI_ATZERES || yomTovIndex == JewishCalendar.SIMCHAS_TORAH;
    }

    public boolean isTisha() {
        return getYomTovIndex() == JewishCalendar.TISHA_BEAV;
    }

    public boolean isErevChanuka() {
        return getjTomorrow().isChanukah();
    }

    public boolean isSelichos() {
        return isTaanis() && getJewishMonth() != TISHREI && getJewishMonth() != AV;
    }

    public boolean isHatzmaus() {
        if (!sharedPref.getBoolean("modern", true))
            return false;
        return getYomTovIndex() == YOM_HAATZMAUT;
    }

    public boolean isYomYerushaleim() {
        if (!sharedPref.getBoolean("modern", true))
            return false;
        return getYomTovIndex() == YOM_YERUSHALAYIM;
    }

    public boolean isHallel() {
        return (isChanukah() || isRoshChodesh() || isPesach() || isHatzmaus() || isYomYerushaleim() || isSuccos());
    }

    public boolean isErevTisha() {
        if (getjTomorrow().getYomTovIndex() == TISHA_BEAV)
            return true;
        return false;
    }

    public boolean eruvTavshilin() {
        int yomTovIndex = getYomTovIndex();
        if (getDayOfOmer() == 5 || yomTovIndex == JewishCalendar.EREV_PESACH || yomTovIndex == JewishCalendar.EREV_SHAVUOS || yomTovIndex == JewishCalendar.EREV_ROSH_HASHANA || yomTovIndex == JewishCalendar.EREV_SUCCOS || yomTovIndex == JewishCalendar.HOSHANA_RABBA) {
            if (getDayOfWeek() == 5)
                return true;
            if (getDayOfWeek() == 4 && (yomTovIndex == JewishCalendar.EREV_ROSH_HASHANA || !sharedPref.getBoolean("inIsrael", false)))
                return true;
            return false;
        }
        return false;
    }

    public boolean LeapYear(int y) {
        int theYear;
        theYear = y;


        if (theYear < 100) {
            if (theYear > 40) {
                theYear = theYear + 1900;
            } else {
                theYear = theYear + 2000;
            }
        }

        if (theYear % 4 == 0) {
            if (theYear % 100 != 0) {
                //System.out.println("IT IS A LEAP YEAR");
                return true;

            } else if (theYear % 400 == 0) {
                //System.out.println("IT IS A LEAP YEAR");
                return true;
            } else {
                // System.out.println("IT IS NOT A LEAP YEAR");
                return false;
            }
        } else {
            //System.out.println("IT IS NOT A LEAP YEAR");
            return false;
        }
    }

    public boolean isCandelLight() {
        return (getDayOfWeek() == 6) || getYomTovIndex() == JewishCalendar.EREV_PESACH || getYomTovIndex() == JewishCalendar.EREV_SUCCOS || getYomTovIndex() == JewishCalendar.HOSHANA_RABBA || getYomTovIndex() == JewishCalendar.EREV_YOM_KIPPUR || getYomTovIndex() == JewishCalendar.EREV_SHAVUOS || getYomTovIndex() == JewishCalendar.EREV_ROSH_HASHANA || (getJewishMonth() == 1 && getJewishDayOfMonth() == 20);
    }

    public int getParsha() {
        int parshaIndex;
        JewishCalendar parshaDate = (JewishCalendar) clone();
        while (parshaDate.getParshaIndex() < 0) {
            parshaDate.forward();
        }
        parshaIndex = parshaDate.getParshaIndex();
        if (parshaIndex > 52) {
            parshaIndex = doubleParshaFixer(parshaIndex);
        }
        return parshaIndex;
    }

    public boolean vezosHaBracha() {
        if (getParsha() == 0 && getJewishDayOfMonth() < 22)
            return true;
        return false;
    }

    public DaveningInfo getParshaDate() {
        DaveningInfo parshaDate = (DaveningInfo) this.clone();
        while (parshaDate.getParshaIndex() < 1)
            parshaDate.forward();
        return parshaDate;
    }

    public boolean isPesach() {
        return (getYomTovIndex() == JewishCalendar.PESACH || getYomTovIndex() == JewishCalendar.CHOL_HAMOED_PESACH);
    }

    public boolean isSuccos() {
        return (getYomTovIndex()==JewishCalendar.HOSHANA_RABBA||getYomTovIndex() == JewishCalendar.SUCCOS || getYomTovIndex() == JewishCalendar.CHOL_HAMOED_SUCCOS);
    }

    public boolean isEseresYemeiTeshuva() {
        return (getJewishMonth() == JewishCalendar.TISHREI && getJewishDayOfMonth() < 10);
    }

    public String getDailyPasuk() {
        int yomTovIndex = getYomTovIndex();
        String string = "בְּכָל הַמָּקוֹם אֲשֶׁר אַזְכִּיר אֶת שְׁמִי אָבוֹא אֵלֶיךָ וּבֵרַכְתִּיךָ";

        if (isRoshChodesh())
            string = "רָאֹשֵׁי חֳדשִׁים לְעַמְּךָ נָתָתָּ, זְמַן כַּפָּרָה לְכָל תּוֹלְדוֹתָם";

        if (isPesach() || yomTovIndex == JewishCalendar.EREV_PESACH)
            string = "וְהִרְבֵּיתִי אֶת אֹתֹתַי וְאֶת מוֹפְתַי בְּאֶרֶץ מִצְרָיִם";

        if (getDayOfOmer() == 5)
            string = "אָז יָשִׁיר מֹשֶׁה וּבְנֵי יִשְׂרָאֵל אֶת הַשִּׁירָה הַזֹּאת";

        if (yomTovIndex == JewishCalendar.YOM_HASHOAH || yomTovIndex == JewishCalendar.YOM_HAZIKARON)
            string = "גַּם כִּי-אֵלֵךְ בְּגֵיא צַלְמָוֶת, לֹא-אִירָא רָע- כִּי-אַתָּה עִמָּדִי";

        if (yomTovIndex == JewishCalendar.YOM_HAATZMAUT)
            string = "מֵאֵת יְהוָה הָיְתָה זֹּאת הִיא נִפְלָאת בְּעֵינֵינוּ";

        if (yomTovIndex == JewishCalendar.YOM_YERUSHALAYIM)
            string = "הַזֹּרְעִים בְּדִמְעָה בְּרִנָּה יִקְצֹרוּ";

        if (yomTovIndex == JewishCalendar.EREV_SHAVUOS)
            string = "  לְמַעַן הוֹדִעֲךָ כִּי לֹא עַל הַלֶּחֶם לְבַדּוֹ יִחְיֶה הָאָדָם כִּי עַל כָּל מוֹצָא פִי יהוה יִחְיֶה הָאָדָם";

        if (isTaanis())
            string = "הֲשִׁיבֵנוּ יְהוָה אֵלֶיךָ וְנָשׁוּבָה חַדֵּשׁ יָמֵינוּ כְּקֶדֶם";

        if (yomTovIndex == JewishCalendar.FAST_OF_ESTHER)
            string = "אֵבֶל גָּדוֹל לַיְּהוּדִים וְצוֹם וּבְכִי וּמִסְפֵּד שַׂק וָאֵפֶר יֻצַּע לָרַבִּים";

        if (yomTovIndex == JewishCalendar.TU_BEAV)
            string = "אַחַת הִיא יוֹנָתִי תַמָּתִי אַחַת הִיא לְאִמָּהּ בָּרָה הִיא לְיוֹלַדְתָּהּ רָאוּהָ בָנוֹת וַיְאַשְּׁרוּהָ מְלָכוֹת וּפִילַגְשִׁים וַיְהַלְלוּהָ";

        if (yomTovIndex == JewishCalendar.EREV_ROSH_HASHANA)
            string = "יְהוָה יִמְלֹךְ לְעֹלָם וָעֶד";

        if (isEseresYemeiTeshuva())
            string = "דִּרְשׁוּ יְהוָה בְּהִמָּצְאוֹ קְרָאֻהוּ בִּהְיוֹתוֹ קָרוֹב";

        if (yomTovIndex == JewishCalendar.EREV_YOM_KIPPUR)
            string = "כִּי בַיּוֹם הַזֶּה יְכַפֵּר עֲלֵיכֶם, לְטַהֵר אֶתְכֶם מִכֹּל חַטֹּאתֵיכֶם, לִפְנֵי יהוה תִּטְהָרוּ";

        if (isSuccos())
            string = "וְשָׂמַחְתָּ בְּחַגֶּךָ, אַתָּה וּבִנְךָ וּבִתֶּךָ וְעַבְדְּךָ וַאֲמָתֶךָ וְהַלֵּוִי וְהַגֵּר וְהַיָּתוֹם וְהָאַלְמָנָה אֲשֶׁר בִּשְׁעָרֶיךָ";

        if (isChanukah())
            string = "וַיֹּאמֶר אֱלֹהִים יְהִי אוֹר וַיְהִי אוֹר";

        if (yomTovIndex == JewishCalendar.TU_BESHVAT)
            string = "וְהָיָה כְּעֵץ שָׁתוּל עַל פַּלְגֵי מָיִם אֲשֶׁר פִּרְיוֹ יִתֵּן בְּעִתּוֹ וְעָלֵהוּ לֹא יִבּוֹל וְכֹל אֲשֶׁר יַעֲשֶׂה יַצְלִיחַ";

        if (getJewishDayOfMonth() == 7 && getJewishMonth() == JewishCalendar.ADAR)
            string = "וְלֹא קָם נָבִיא עוֹד בְּיִשְׂרָאֵל כְּמֹשֶׁה אֲשֶׁר יְדָעוֹ יְהוָה פָּנִים אֶל פָּנִים";

        if (yomTovIndex == PURIM_KATAN)
            string = "וִימֵי הַפּוּרִים הָאֵלֶּה לֹא יַעַבְרוּ מִתּוֹךְ הַיְּהוּדִים, וְזִכְרָם לֹא יָסוּף מִזַּרְעָם";

        if (yomTovIndex == JewishCalendar.PURIM)
            string = "לַיְּהוּדִים הָיְתָה אוֹרָה וְשִׂמְחָה וְשָׂשֹׂן וִיקָר";

        if (yomTovIndex == JewishCalendar.SHUSHAN_PURIM)
            string = "וַיֹּאמֶר הַמֶּלֶךְ לְאֶסְתֵּר בְּמִשְׁתֵּה הַיַּיִן מַה שְּׁאֵלָתֵךְ וְיִנָּתֵן לָךְ וּמַה בַּקָּשָׁתֵךְ עַד חֲצִי הַמַּלְכוּת וְתֵעָשׂ";

        return string;
    }

    public boolean isMussaf(){
        return isPesach()||isRoshChodesh()||isSuccos();
    }

    public Calendar getGregorianCalendar(){
        Calendar cal = Calendar.getInstance();
        cal.set(getGregorianYear(), getGregorianMonth(), getGregorianDayOfMonth());
        return cal;
    }

    public Context getContext(){
        return this.context;
    }

    private int doubleParshaFixer(int parshaIndex) {
        switch (parshaIndex) {
            case 53:
                parshaIndex = 21;
                break;
            case 54:
                parshaIndex = 26;
                break;
            case 55:
                parshaIndex = 28;
                break;
            case 56:
                parshaIndex = 31;
                break;
            case 57:
                parshaIndex = 38;
                break;
            case 58:
                parshaIndex = 41;
                break;
            case 59:
                parshaIndex = 50;
                break;
        }
        return parshaIndex;
    }


}
