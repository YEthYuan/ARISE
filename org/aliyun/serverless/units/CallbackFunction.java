package org.aliyun.serverless.units;

import java.util.concurrent.Callable;

public abstract class CallbackFunction<PARA_TYPE, RETURN_TYPE> implements Callable<RETURN_TYPE> {
    protected PARA_TYPE parameter;

    public void setParameter(PARA_TYPE parameter) {
        this.parameter = parameter;
    }

    public PARA_TYPE getParameter() {
        return this.parameter;
    }

    @Override
    public abstract RETURN_TYPE call() throws Exception;
}

