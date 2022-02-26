// file: WDS_Ontology.java generated by ontology bean generator
package simulator.ontology;

import jade.content.onto.*;
import jade.content.schema.*;

/** file: WDS_Ontology.java
 * @author ontology bean generator
 * @author Ioannis N. Athanasiadis
 * @version Oct 2003 - 2006
 */

public class WDS_Ontology extends Ontology{

  //NAME
  public static final String ONTOLOGY_NAME = "WDS_Ontology";
  private static WDS_Ontology theInstance = new WDS_Ontology(ACLOntology.getInstance());
  public static WDS_Ontology getInstance() {
     return theInstance;
  }
  //VOCABULARY
  public static final String PARAMETER="Parameter";

  public static final String PARAMETER_WEIGHT="weight";
  public static final String PARAMETER_NAME="name";  public static final String SAVEPERSONALDATA="SavePersonalData";

  public static final String SAVEPERSONALDATA_TODIRECTORY="toDirectory";  public static final String LAUNCHGUI="LaunchGUI";
  public static final String SAVINGPATH="SavingPath";

  public static final String SAVINGPATH_DIRECTORY="directory";  public static final String START="Start";

  public static final String START_SIMULATIONSTEP="simulationStep";  public static final String METDATA="MetData";

  public static final String METDATA_RAINFALL="rainfall";
  public static final String METDATA_TEMPERATURE="temperature";  public static final String STEPATTR="StepAttr";

  public static final String STEPATTR_ID="id";  public static final String WATERCONSUMPTION="WaterConsumption";

  public static final String WATERCONSUMPTION_QUANTITY="quantity";  public static final String PRICEBLOCK="PriceBlock";

  public static final String PRICEBLOCK_PRICE="price";
  public static final String PRICEBLOCK_NO="no";
  public static final String PRICEBLOCK_LIMITUP="limitUp";
  public static final String PRICEBLOCK_LIMITDOWN="limitDown";  public static final String CONSUMES="Consumes";

  public static final String CONSUMES_PERSONALCONSUMPTION="personalConsumption";
  public static final String CONSUMES_METEODATA="meteoData";
  public static final String CONSUMES_PRICINGSCALE="pricingScale";
  public static final String CONSUMES_STEP2="step2";  public static final String ASKWEIGHTSFOR="AskWeightsFor";

  public static final String ASKWEIGHTSFOR_PARAMETERS="parameters";
  public static final String ASKWEIGHTSFOR_STEP3="step3";  public static final String STEPTOTALCONSUMPTION="StepTotalConsumption";

  public static final String STEPTOTALCONSUMPTION_STEPCONSUMPTION="stepConsumption";
  public static final String STEPTOTALCONSUMPTION_WATERPRICE="waterPrice";
  public static final String STEPTOTALCONSUMPTION_STEP4="step4";  public static final String HASMETDATA="HasMetData";

  public static final String HASMETDATA_STEP1="step1";
  public static final String HASMETDATA_DATA="data";

  /**
   * Constructor
  */
  private WDS_Ontology(Ontology base){
   super(ONTOLOGY_NAME, base);
   try {
    // adding Concept(s)
    ConceptSchema parameterSchema = new ConceptSchema(PARAMETER);
    add(parameterSchema, Parameter.class);
    ConceptSchema savePersonalDataSchema = new ConceptSchema(SAVEPERSONALDATA);
    add(savePersonalDataSchema, SavePersonalData.class);
    ConceptSchema launchGUISchema = new ConceptSchema(LAUNCHGUI);
    add(launchGUISchema, LaunchGUI.class);
    ConceptSchema savingPathSchema = new ConceptSchema(SAVINGPATH);
    add(savingPathSchema, SavingPath.class);
    ConceptSchema startSchema = new ConceptSchema(START);
    add(startSchema, Start.class);
    ConceptSchema metDataSchema = new ConceptSchema(METDATA);
    add(metDataSchema, MetData.class);
    ConceptSchema stepAttrSchema = new ConceptSchema(STEPATTR);
    add(stepAttrSchema, StepAttr.class);
    ConceptSchema waterConsumptionSchema = new ConceptSchema(WATERCONSUMPTION);
    add(waterConsumptionSchema, WaterConsumption.class);
    ConceptSchema priceBlockSchema = new ConceptSchema(PRICEBLOCK);
    add(priceBlockSchema, PriceBlock.class);
    // adding Predicate(s)
    PredicateSchema consumesSchema = new PredicateSchema(CONSUMES);
    add(consumesSchema, Consumes.class);
    PredicateSchema askWeightsForSchema = new PredicateSchema(ASKWEIGHTSFOR);
    add(askWeightsForSchema, AskWeightsFor.class);
    PredicateSchema stepTotalConsumptionSchema = new PredicateSchema(STEPTOTALCONSUMPTION);
    add(stepTotalConsumptionSchema, StepTotalConsumption.class);
    PredicateSchema hasMetDataSchema = new PredicateSchema(HASMETDATA);
    add(hasMetDataSchema, HasMetData.class);


    parameterSchema.add(PARAMETER_WEIGHT, (TermSchema)getSchema(BasicOntology.FLOAT) , ObjectSchema.OPTIONAL);
    // facets of weight:
    parameterSchema.add(PARAMETER_NAME, (TermSchema)getSchema(BasicOntology.STRING) , ObjectSchema.OPTIONAL);
    // facets of name:
    savePersonalDataSchema.add(SAVEPERSONALDATA_TODIRECTORY, savingPathSchema , ObjectSchema.OPTIONAL);
    // facets of toDirectory: MinimumCardinality 1,MaximumCardinality 1, getAllowedClses: Cls(SavingPath), , getAllowedParents: Cls(SavingPath), , getAllowedValues: Cls(SavingPath),
    savingPathSchema.add(SAVINGPATH_DIRECTORY, (TermSchema)getSchema(BasicOntology.STRING) , ObjectSchema.OPTIONAL);
    // facets of directory:
    startSchema.add(START_SIMULATIONSTEP, stepAttrSchema , ObjectSchema.OPTIONAL);
    // facets of simulationStep: MinimumCardinality 1,MaximumCardinality 1, getAllowedClses: Cls(StepAttr), , getAllowedParents: Cls(StepAttr), , getAllowedValues: Cls(StepAttr),
    metDataSchema.add(METDATA_RAINFALL, (TermSchema)getSchema(BasicOntology.FLOAT) , ObjectSchema.OPTIONAL);
    // facets of rainfall:
    metDataSchema.add(METDATA_TEMPERATURE, (TermSchema)getSchema(BasicOntology.FLOAT) , ObjectSchema.OPTIONAL);
    // facets of temperature:
    stepAttrSchema.add(STEPATTR_ID, (TermSchema)getSchema(BasicOntology.INTEGER) , ObjectSchema.OPTIONAL);
    // facets of id:
    waterConsumptionSchema.add(WATERCONSUMPTION_QUANTITY, (TermSchema)getSchema(BasicOntology.FLOAT) , ObjectSchema.OPTIONAL);
    // facets of quantity:
    priceBlockSchema.add(PRICEBLOCK_PRICE, (TermSchema)getSchema(BasicOntology.FLOAT) , ObjectSchema.OPTIONAL);
    // facets of price:
    priceBlockSchema.add(PRICEBLOCK_NO, (TermSchema)getSchema(BasicOntology.INTEGER) , ObjectSchema.OPTIONAL);
    // facets of no:
    priceBlockSchema.add(PRICEBLOCK_LIMITUP, (TermSchema)getSchema(BasicOntology.INTEGER) , ObjectSchema.OPTIONAL);
    // facets of limitUp:
    priceBlockSchema.add(PRICEBLOCK_LIMITDOWN, (TermSchema)getSchema(BasicOntology.INTEGER) , ObjectSchema.OPTIONAL);
    // facets of limitDown:
    consumesSchema.add(CONSUMES_PERSONALCONSUMPTION, waterConsumptionSchema , ObjectSchema.OPTIONAL);
    // facets of personalConsumption: MaximumCardinality 1, getAllowedClses: Cls(WaterConsumption), , getAllowedParents: Cls(WaterConsumption), , getAllowedValues: Cls(WaterConsumption),
    consumesSchema.add(CONSUMES_METEODATA, metDataSchema , ObjectSchema.OPTIONAL);
    // facets of meteoData: MinimumCardinality 1,MaximumCardinality 1, getAllowedClses: Cls(MetData), , getAllowedParents: Cls(MetData), , getAllowedValues: Cls(MetData),
    consumesSchema.add(CONSUMES_PRICINGSCALE, (AggregateSchema)getSchema(BasicOntology.SET) , ObjectSchema.OPTIONAL);
    // facets of pricingScale: MinimumCardinality 1,getAllowedClses: Cls(PriceBlock), , getAllowedParents: Cls(PriceBlock), , getAllowedValues: Cls(PriceBlock),
    consumesSchema.add(CONSUMES_STEP2, stepAttrSchema , ObjectSchema.OPTIONAL);
    // facets of step2: MinimumCardinality 1,MaximumCardinality 1, getAllowedClses: Cls(StepAttr), , getAllowedParents: Cls(StepAttr), , getAllowedValues: Cls(StepAttr),
    askWeightsForSchema.add(ASKWEIGHTSFOR_PARAMETERS, (AggregateSchema)getSchema(BasicOntology.SET) , ObjectSchema.OPTIONAL);
    // facets of parameters: getAllowedClses: Cls(Parameter), , getAllowedParents: Cls(Parameter), , getAllowedValues: Cls(Parameter),
    askWeightsForSchema.add(ASKWEIGHTSFOR_STEP3, stepAttrSchema , ObjectSchema.OPTIONAL);
    // facets of step3: MinimumCardinality 1,MaximumCardinality 1, getAllowedClses: Cls(StepAttr), , getAllowedParents: Cls(StepAttr), , getAllowedValues: Cls(StepAttr),
    stepTotalConsumptionSchema.add(STEPTOTALCONSUMPTION_STEPCONSUMPTION, waterConsumptionSchema , ObjectSchema.OPTIONAL);
    // facets of stepConsumption: MinimumCardinality 1,MaximumCardinality 1, getAllowedClses: Cls(WaterConsumption), , getAllowedParents: Cls(WaterConsumption), , getAllowedValues: Cls(WaterConsumption),
    stepTotalConsumptionSchema.add(STEPTOTALCONSUMPTION_WATERPRICE, (AggregateSchema)getSchema(BasicOntology.SET) , ObjectSchema.OPTIONAL);
    // facets of waterPrice: MinimumCardinality 1,getAllowedClses: Cls(PriceBlock), , getAllowedParents: Cls(PriceBlock), , getAllowedValues: Cls(PriceBlock),
    stepTotalConsumptionSchema.add(STEPTOTALCONSUMPTION_STEP4, stepAttrSchema , ObjectSchema.OPTIONAL);
    // facets of step4: MinimumCardinality 1,MaximumCardinality 1, getAllowedClses: Cls(StepAttr), , getAllowedParents: Cls(StepAttr), , getAllowedValues: Cls(StepAttr),
    hasMetDataSchema.add(HASMETDATA_STEP1, stepAttrSchema , ObjectSchema.OPTIONAL);
    // facets of step1: MinimumCardinality 1,MaximumCardinality 1, getAllowedClses: Cls(StepAttr), , getAllowedParents: Cls(StepAttr), , getAllowedValues: Cls(StepAttr),
    hasMetDataSchema.add(HASMETDATA_DATA, metDataSchema , ObjectSchema.OPTIONAL);
    // facets of data: MaximumCardinality 1, getAllowedClses: Cls(MetData), , getAllowedParents: Cls(MetData), , getAllowedValues: Cls(MetData),
   }catch (Exception e) {e.printStackTrace();}
  }
}
