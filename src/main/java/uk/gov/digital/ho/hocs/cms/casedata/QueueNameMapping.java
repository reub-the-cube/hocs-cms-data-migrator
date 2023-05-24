package uk.gov.digital.ho.hocs.cms.casedata;

import java.util.HashMap;

public class QueueNameMapping {

    public static String getCaseType(String queueName) {

        HashMap<String, String> queueNameMapping = new HashMap<>();
        queueNameMapping.put("ExGratia Complex", CaseType.COMP.name());
        queueNameMapping.put("NRO-Yarls Wood", CaseType.BF.name());
        queueNameMapping.put("NRO-CRTS 8", CaseType.COMP.name());
        queueNameMapping.put("CSU-Wales and South West", CaseType.COMP.name());
        queueNameMapping.put("CSU-Immigration Enquiry Bureau (do not use)", CaseType.COMP.name());
        queueNameMapping.put("CSU-Crime Directorate", CaseType.COMP.name());
        queueNameMapping.put("NRO-Harmondsworth", CaseType.COMP.name());
        queueNameMapping.put("NRO-CRTS 6", CaseType.COMP.name());
        queueNameMapping.put("HMPO Complex", CaseType.POGR.name());
        queueNameMapping.put("NRO-NEYH Pending", CaseType.COMP.name());
        queueNameMapping.put("Windrush Complaints", CaseType.COMP.name());
        queueNameMapping.put("OoSS Allocations CCT", CaseType.COMP.name());
        queueNameMapping.put("UKVI", CaseType.COMP.name());
        queueNameMapping.put("CSU-Border Force", CaseType.BF.name());
        queueNameMapping.put("CSU-Detention", CaseType.IEDET.name());
        queueNameMapping.put("HMPO TRT", CaseType.POGR.name());
        queueNameMapping.put("Complaints OoSS", CaseType.COMP.name());
        queueNameMapping.put("CSU-EUSS", CaseType.COMP.name());
        queueNameMapping.put("Allocations", CaseType.COMP.name());
        queueNameMapping.put("CSU-NE Yorkshire and Humber", CaseType.COMP.name());
        queueNameMapping.put("CSU-Case Resolution Directorate", CaseType.COMP.name());
        queueNameMapping.put("CSU-Sheffield Call Centre", CaseType.COMP.name());
        queueNameMapping.put("PHSO", CaseType.COMP.name());
        queueNameMapping.put("NRO-LIT Hounslow Richmond and Kingston", CaseType.COMP.name());
        queueNameMapping.put("RH-NE Ombudsman ExG", CaseType.COMP.name());
        queueNameMapping.put("CSU-Scotland and NI", CaseType.COMP.name());
        queueNameMapping.put("NRO-Morton Hall", CaseType.IEDET.name());
        queueNameMapping.put("HMPO", CaseType.POGR.name());
        queueNameMapping.put("NRO-WICU (BF)", CaseType.BF.name());
        queueNameMapping.put("Business priority OoSS", CaseType.COMP.name());
        queueNameMapping.put("NRO-Brook House", CaseType.COMP.name());
        queueNameMapping.put("NRO-MEEI Quality Assurance", CaseType.COMP.name());
        queueNameMapping.put("Surge Team", CaseType.COMP.name());
        queueNameMapping.put("CSU-Midland and East", CaseType.COMP.name());
        queueNameMapping.put("CSU-NW Region", CaseType.COMP.name());
        queueNameMapping.put("CSU-Criminal Casework Directorate", CaseType.COMP.name());
        queueNameMapping.put("LSE-Pending", CaseType.COMP.name());
        queueNameMapping.put("CSU-London And SE", CaseType.COMP.name());
        queueNameMapping.put("RH-International", CaseType.COMP.name());
        queueNameMapping.put("HMPO awaiting customer", CaseType.POGR.name());
        queueNameMapping.put("ExGratia Straightforward", CaseType.COMP.name());
        queueNameMapping.put("NRO-MEE and International", CaseType.COMP.name());
        queueNameMapping.put("Escalations CCT", CaseType.COMP.name());
        queueNameMapping.put("Asylum Protection Hub Pilot", CaseType.COMP.name());

        return queueNameMapping.get(queueName);
    }
}
