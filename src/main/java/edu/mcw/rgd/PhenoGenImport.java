package edu.mcw.rgd;

import edu.mcw.rgd.datamodel.Gene;
import edu.mcw.rgd.datamodel.SpeciesType;
import edu.mcw.rgd.datamodel.XdbId;
import edu.mcw.rgd.log.RGDSpringLogger;
import edu.mcw.rgd.process.Utils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author jdepons
 * @since 4/26/12
 */
public class PhenoGenImport {

    private PhenoGenDAO dao = new PhenoGenDAO();
    private String version;

    Logger log = Logger.getLogger("core");
    private String srcPipeline;

    public static void main(String[] args) throws Exception {

        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(bf).loadBeanDefinitions(new FileSystemResource("properties/AppConfigure.xml"));
        PhenoGenImport manager = (PhenoGenImport) (bf.getBean("manager"));

        try {
            manager.run();
        }catch (Exception e) {
            manager.log.error(e);
            throw e;
        }
    }

    public void run() throws Exception {
        log.info(getVersion());
        log.info("  "+dao.getConnectionInfo());
        SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.info("   started at "+sdt.format(new Date()));

        run(SpeciesType.RAT);
        run(SpeciesType.MOUSE);
    }

    public void run(int speciesTypeKey) throws Exception {

        long time0 = System.currentTimeMillis();

        String species = SpeciesType.getCommonName(speciesTypeKey);
        log.info("=== "+species);

        // QC
        log.debug("  QC: get PhenoGen Ids in RGD for "+species);
        List<XdbId> idsInRgd = dao.getPhenoGenXdbIds(speciesTypeKey, getSrcPipeline());
        log.debug("  QC: get incoming PhenoGen Ids for "+species);
        List<XdbId> idsIncoming = getIncomingIds(speciesTypeKey);

        // determine to-be-inserted PhenoGen ids
        log.debug("  QC: determine to-be-inserted PhenoGen Ids");
        List<XdbId> idsToBeInserted = removeAll(idsIncoming, idsInRgd);

        // determine matching PhenoGen ids
        log.debug("  QC: determine matching PhenoGen Ids");
        List<XdbId> idsMatching = retainAll(idsInRgd, idsIncoming);

        // determine to-be-deleted cosmic ids
        log.debug("  QC: determine to-be-deleted PhenoGen Ids");
        List<XdbId> idsToBeDeleted = removeAll(idsInRgd, idsIncoming);


        // loading
        if( !idsToBeInserted.isEmpty() ) {
            log.info("inserting PhenoGene ids for "+species+": "+idsToBeInserted.size());
            dao.insertXdbs(idsToBeInserted);
        }

        if( !idsToBeDeleted.isEmpty() ) {
            log.info("deleting PhenoGen ids for "+species+": "+idsToBeDeleted.size());
            dao.deleteXdbIds(idsToBeDeleted);
        }

        if( !idsMatching.isEmpty() ) {
            log.info("matching PhenoGen ids for "+species+": "+idsMatching.size());
            dao.updateModificationDate(idsMatching);
        }

        logSummaryIntoRgdSpringLogger(idsMatching.size()+idsToBeDeleted.size()-idsToBeDeleted.size(), species);

        log.info("=== Load OK for "+species+"; elapsed "+ Utils.formatElapsedTime(time0, System.currentTimeMillis()));
    }

    List<XdbId> removeAll(List<XdbId> ids, List<XdbId> idsToBeRemoved) {
        Set<XdbId> idsToBeRemovedSet = new HashSet<XdbId>(idsToBeRemoved);
        List<XdbId> result = new ArrayList<XdbId>(ids);
        result.removeAll(idsToBeRemovedSet);
        return result;
    }

    List<XdbId> retainAll(List<XdbId> list1, List<XdbId> list2) {
        Set<XdbId> idsToBeRetained = new HashSet<XdbId>(list2);
        List<XdbId> result = new ArrayList<XdbId>(list1);
        result.retainAll(idsToBeRetained);
        return result;
    }

    void logSummaryIntoRgdSpringLogger(int phenoGenIdsTotal, String species) throws Exception {

        RGDSpringLogger rgdLogger = new RGDSpringLogger();
        String subsystem = "PhenoGen"+species;
        rgdLogger.log(subsystem, "PhenoGenIdsTotal", phenoGenIdsTotal);
    }

    List<XdbId> getIncomingIds(int speciesTypeKey) throws Exception {

        List<Gene> genes = dao.getActiveGenes(speciesTypeKey);
        List<XdbId> incomingIds = new ArrayList<XdbId>(genes.size());
        for (Gene g: genes) {
            XdbId x = new XdbId();
            x.setAccId(g.getSymbol());
            x.setSrcPipeline(getSrcPipeline());
            x.setRgdId(g.getRgdId());
            x.setXdbKey(51);
            x.setCreationDate(new Date());
            x.setModificationDate(x.getCreationDate());
            x.setLinkText(g.getSymbol());
            incomingIds.add(x);
        }
        return incomingIds;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setSrcPipeline(String srcPipeline) {
        this.srcPipeline = srcPipeline;
    }

    public String getSrcPipeline() {
        return srcPipeline;
    }
}

