/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Session;

/**
 *
 * @author Ulka
 */
public final class Menadzer {

    private static Menadzer instance = null;
    private ArrayList<Sprzet> gniazdka, czujniki;
    private int currIdPomTemp = 0, currIdPomGniazdka = 0;
    private final Session session;

    private static final String QUERY_GNIAZDKA = "from Sprzet where typ = 'gniazdko'";
    private static final String QUERY_CZUJNIKI = "from Sprzet where typ = 'czujnik'";
    private static final String QUERY_MAX_POMIARY_TEMP = "select max(p.pomiarId) from PomiarTemperatura p";
    private static final String QUERY_MAX_POMIARY_GNIAZKA = "select max(p.pomiarId) from PomiarGniazdko p";
    private static final String OUERY_OSTATNIE_POM_TEMP = "from PomiarTemperatura pom\n"
            + "where  pom.momentPomiaru = (select max(pom2.momentPomiaru) from PomiarTemperatura pom2 "
            + "where pom.sprzetId = pom2.sprzetId)";
    private static final String OUERY_OSTATNIE_POM_GNIAZDKA = "from PomiarGniazdko pom\n"
            + "where  pom.momentPomiaru = (select max(pom2.momentPomiaru) from PomiarGniazdko pom2 "
            + "where pom.sprzetId = pom2.sprzetId)";
    private static final String QUERY_MAX_GNIAZDKA = "select max(s.sprzetId) from Sprzet s where typ = 'gniazdko'";
    private static final String QUERY_MAX_CZUJNIKI = "select max(s.sprzetId) from Sprzet s where typ = 'czujnik'";
    private static final String QUERY_MAX_SPRZET = "select max(s.sprzetId) from Sprzet s";

    private Menadzer() {
        session = HibernateUtil.getSessionFactory().openSession();
        currIdPomTemp = HibernateUtil.executeHQLUniqueQuery(QUERY_MAX_POMIARY_TEMP);
        currIdPomGniazdka = HibernateUtil.executeHQLUniqueQuery(QUERY_MAX_POMIARY_GNIAZKA);
        System.out.println("CurrIDs: " + currIdPomTemp + ", " + currIdPomGniazdka);
        refreshSprzet();
    }

    public static Menadzer getInstance() {
        if (instance == null) {
            instance = new Menadzer();
        }
        return instance;
    }

    public ArrayList<Sprzet> getGniazdka() {
        return gniazdka;
    }

    public ArrayList<Sprzet> getCzujniki() {
        return czujniki;
    }
    
    public void setWlaczSprzet(int idSprzet, int czyWlaczony) {
        String hql = "update Sprzet set czyWlaczony = " + czyWlaczony + " where sprzetId = " + idSprzet;
        System.out.println(hql);
        System.out.println("Wylaczanie czujnika: " + idSprzet);
        HibernateUtil.executeHQLUpdate(hql);
    }
    
    public void refreshSprzet() {
        List resultCzujniki = HibernateUtil.executeHQLListQuery(QUERY_CZUJNIKI);
        List resultGniazdka = HibernateUtil.executeHQLListQuery(QUERY_GNIAZDKA);

        czujniki = new ArrayList<>();
        for (Object o : resultCzujniki) {
            Sprzet s = (Sprzet) o;
            czujniki.add(s);
        }

        gniazdka = new ArrayList<>();
        for (Object o : resultGniazdka) {
            Sprzet s = (Sprzet) o;
            gniazdka.add(s);
        }
    }

    public void saveSprzet(Sprzet s) {
        HibernateUtil.executeHQLsave(s);
    }
    
    public void deleteSprzet(Sprzet s){
        String hql = null; 
        String hql2 = null;
        if ("czujnik".equals(s.getTyp())) {
            hql = "delete PomiarTemperatura where sprzetId = " + s.getSprzetId();
        }
        if ("gniazdko".equals(s.getTyp())) {
            hql = "delete PomiarGniazdko where sprzetId = " + s.getSprzetId();
        }
        hql2 = "delete Sprzet where sprzetId = " + s.getSprzetId();
        if (hql != null) {
            HibernateUtil.executeHQLDelete(hql);
        }
        System.out.println("Usuwanie: " + hql);
        HibernateUtil.executeHQLDelete(hql2);
        System.out.println("Usuwanie: " + hql2);
    }

    public PomiarTemperatura newPomiarTemp(int id, BigDecimal wynik) {
        currIdPomTemp++;
        Sprzet s = null;
        for (Sprzet spr : czujniki) {
            if (spr.getSprzetId() == id) {
                s = spr;
            } 
        }
        if (s != null) {
            PomiarTemperatura nowyPomiar = new PomiarTemperatura(currIdPomTemp, s.getSprzetId(), wynik);
            //HibernateUtil.executeHQLsave(session, nowyPomiar);
            HibernateUtil.executeHQLsave(nowyPomiar);
            System.out.println(nowyPomiar);
            return nowyPomiar;
        }
        return null;

    }

    public PomiarGniazdko newPomiarGniazdko(int id, BigDecimal wynikNap, BigDecimal wynikPrad, BigDecimal wynikMoc) {
        currIdPomGniazdka++;
        Sprzet s2 = null;
        for (Sprzet spr : gniazdka) {
            if (spr.getSprzetId() == id) {
                s2 = spr;
            }
        }
        if (s2 != null) {
            PomiarGniazdko nowyPomiar = new PomiarGniazdko(currIdPomGniazdka, s2.getSprzetId(),
                    wynikNap, wynikPrad, wynikMoc);
            HibernateUtil.executeHQLsave(nowyPomiar);
            System.out.println(nowyPomiar);
            return nowyPomiar;
        }
        return null;
    }

    public String findOstatniPomiarTempFor(int sprzetId) {
        List resultTemp = HibernateUtil.executeHQLListQuery(OUERY_OSTATNIE_POM_TEMP);
        String wynik = "";
        if (!resultTemp.isEmpty()) {
            for (Object o : resultTemp) {
                PomiarTemperatura s = (PomiarTemperatura) o;
                if (s.getSprzetId() == sprzetId) {
                    wynik = s.getPomiarTemp().toString();
                }
            }
        }
        return wynik;

    }

    public PomiarGniazdko findOstatniPomiarGniazdkoFor(int sprzetId) {
        List resultGniazdko = HibernateUtil.executeHQLListQuery(OUERY_OSTATNIE_POM_GNIAZDKA);
        PomiarGniazdko wynik = null;
        if (!resultGniazdko.isEmpty()) {
            for (Object o : resultGniazdko) {
                wynik = (PomiarGniazdko) o;
                if (wynik.getSprzetId() == sprzetId) {
                    return wynik;
                }
            }
        }
        //System.out.println("Dla gniazdka " + sprzetId + " znaleziono pomiar (moc)" + wynik.getPomiarMoc());
        return wynik;
    }

    public int findOstatniCzujnikId() {
        return HibernateUtil.executeHQLUniqueQuery(QUERY_MAX_CZUJNIKI);
    }

    public int findOstatniGniazdkoId() {
        return HibernateUtil.executeHQLUniqueQuery(QUERY_MAX_GNIAZDKA);
    }
    
    public int findOstatniSprzetId() {
        return HibernateUtil.executeHQLUniqueQuery(QUERY_MAX_SPRZET);
    }
}
