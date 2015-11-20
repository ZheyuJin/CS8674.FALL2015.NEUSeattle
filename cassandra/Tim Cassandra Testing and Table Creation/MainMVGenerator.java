package fortesting;

/**
 * creates new usecase #2 and #3 tables, requires proceduresstats,
 * proceduresinfo, and providers tables to be fully loaded
 * 
 * a bug in MVPatientResponsibility creates 5 bad entries. I will fix this
 * later.
 * 
 * @author tim
 *
 */
public class MainMVGenerator {

    public static void main(String[] args) {
        MVPatientResponsibility.createPatientResponsibilityMV();

    }

}
