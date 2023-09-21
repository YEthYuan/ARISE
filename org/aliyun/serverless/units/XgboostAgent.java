package org.aliyun.serverless.units;

import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.aliyun.serverless.config.ParamsManager;

import java.util.logging.Logger;

public class XgboostAgent {
    public enum XgboostAgentType {
        ASSIGN,
        IDLE
    }

    private static final Logger logger = Logger.getLogger(XgboostAgent.class.getName());
    private static final ParamsManager paramsManager = ParamsManager.getManager();

    private Booster assignModel;
    private Booster idleModel;

    private final String metaKey;

    public XgboostAgent(String metaKey) throws XGBoostError {
        this.metaKey = metaKey;
        this.loadModel();
    }

    // debug constructor, DO NOT USE!
    private XgboostAgent(String metaKey, String assignModelPath, String idleModelPath) throws XGBoostError {
        this.metaKey = metaKey;
        this.assignModel = XGBoost.loadModel(assignModelPath);
        this.idleModel = XGBoost.loadModel(idleModelPath);
    }

    private void loadModel() throws XGBoostError {
        String basePathFull = (String) paramsManager.getParamValue("ModelPath.basePathFull");
        String basePathHalf = (String) paramsManager.getParamValue("ModelPath.basePathHalf");

        // assign
        String assignFileName = (String) paramsManager.getParamValue("ModelPath." + this.metaKey + ".assign");
        String assignModelPath = basePathFull + assignFileName;
        this.assignModel = XGBoost.loadModel(assignModelPath);

        // idle
        String idleFileName = (String) paramsManager.getParamValue("ModelPath." + this.metaKey + ".idle");
        String idleModelPath = basePathHalf + idleFileName;
        this.idleModel = XGBoost.loadModel(idleModelPath);
    }

    public Integer predict(int[] windows, XgboostAgentType action) throws XGBoostError {
        int size = windows.length;
        Integer fixedWindowSize = (Integer) paramsManager.getParamValue("general.windowsNum");
        if (size != fixedWindowSize) {
            logger.severe(String.format("Input window size not equal to %d !", fixedWindowSize));
            throw new XGBoostError(String.format("Input window size not equal to %d !", fixedWindowSize));
        }

        float[] modelInputData = new float[size];
        for (int i = 0; i < size; i++) {
            modelInputData[i] = windows[i];
        }
        DMatrix modelInputMatrix = new DMatrix(modelInputData, 1, size);

        Integer res = null;
        if (XgboostAgentType.ASSIGN == action) {
            logger.info("Calling XGBoost to predict assign request ...");
            float[][] predicts = this.assignModel.predict(modelInputMatrix);
            try {
                res = (int) Math.floor(predicts[0][0]);
                res = Math.max(res, 0);
                logger.info(String.format("Assign predict value: %f, decide to assign %d instances!", predicts[0][0], res));
            } catch (Exception e) {
                logger.severe("Failed to predict assign request! " + e.getMessage());
                throw new XGBoostError("Failed to predict assign request!");
            }
        } else if (XgboostAgentType.IDLE == action) {
            logger.info("Calling XGBoost to predict idle request...");
            float[][] predicts = this.idleModel.predict(modelInputMatrix);
            try {
                res = (int) Math.floor(predicts[0][0]);
                res = Math.max(res, 0);
                logger.info(String.format("Idle predict value: %f, decide to idle %d instances!", predicts[0][0], res));
            } catch (Exception e) {
                logger.severe("Failed to predict idle request! " + e.getMessage());
                throw new XGBoostError("Failed to predict idle request!");
            }
        }

        if (null == res) {
            logger.severe("Failed to predict request!");
            throw new XGBoostError("Failed to predict request!");
        }

        return res;
    }
}
