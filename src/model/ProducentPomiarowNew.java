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
public class ProducentPomiarowNew {

    private ArrayList<Sprzet> gniazdka, czujniki;
    private int currIdTemp = 0, currIdGniazdka = 0;
    private Session session;
    
    private static final String QUERY_GNIAZDKA = "from Sprzet where typ = 'gniazdko'";
    private static final String QUERY_CZUJNIKI = "from Sprzet where typ = 'czujnik'";
    private static final String QUERY_MAX_POMIARY_TEMP = "select max(p.pomiarId) from PomiarTemperatura p";
    private static final String QUERY_MAX_POMIARY_GNIAZKA = "select max(p.pomiarId) from PomiarGniazdko p";
    private static final String OUERY_OSTATNIE_POM_TEMP = "from PomiarTemperatura pom\n" +
    "where  pom.momentPomiaru = (select max(pom2.momentPomiaru) from PomiarTemperatura pom2 "
            + "where pom.sprzetId = pom2.sprzetId)";

    public ProducentPomiarowNew() {
        session = HibernateUtil.getSessionFactory().openSession();
        loadData();
        System.out.println("CurrIDs: " + currIdTemp + ", " + currIdGniazdka);
    }

    private void loadData() {
        
        currIdTemp = HibernateUtil.executeHQLUniqueQuery(session, QUERY_MAX_POMIARY_TEMP);
        currIdGniazdka = HibernateUtil.executeHQLUniqueQuery(session, QUERY_MAX_POMIARY_GNIAZKA);
        List resultCzujniki = HibernateUtil.executeHQLListQuery(session, QUERY_CZUJNIKI);
        List resultGniazdka = HibernateUtil.executeHQLListQuery(session, QUERY_GNIAZDKA);

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

    public ArrayList<Sprzet> getGniazdka() {
        return gniazdka;
    }

    public ArrayList<Sprzet> getCzujniki() {
        return czujniki;
    }

    public PomiarTemperatura newPomiarTemp(int id, BigDecimal wynik) {
        currIdTemp++;
        Sprzet s = null;
        for (Sprzet spr : czujniki) {
            if (spr.getSprzetId() == id) s = spr;
        }
        PomiarTemperatura nowyPomiar = new PomiarTemperatura(currIdTemp, s.getSprzetId(), wynik);
        HibernateUtil.executeHQLsave(session, nowyPomiar);
        System.out.println(nowyPomiar);
        return nowyPomiar;
    }
    
    public PomiarGniazdko newPomiarGniazdko(int id, BigDecimal wynikNap, BigDecimal wynikPrad, BigDecimal wynikMoc) {
        currIdGniazdka++;
        Sprzet s2 = null;
        for (Sprzet spr : gniazdka) {
            if (spr.getSprzetId() == id) s2 = spr;
        }
        PomiarGniazdko nowyPomiar =  new PomiarGniazdko(currIdGniazdka, s2.getSprzetId(), 
                wynikNap, wynikPrad, wynikMoc);
        HibernateUtil.executeHQLsave(session, nowyPomiar);
        System.out.println(nowyPomiar);
        return nowyPomiar;
    }

    public String findOstatniPomiarTempFor(int sprzetId) {
        List resultTemp = HibernateUtil.executeHQLListQuery(session, OUERY_OSTATNIE_POM_TEMP);
        String wynik = "";
        //ArrayList<PomiarTemperatura> pomiary = new ArrayList<>();
        for (Object o : resultTemp) {
            PomiarTemperatura s = (PomiarTemperatura) o;
            if (s.getSprzetId() == sprzetId) wynik = s.getPomiarTemp().toString();
            //pomiary.add(s);
        }
        
        return wynik;

    }
}    
    
