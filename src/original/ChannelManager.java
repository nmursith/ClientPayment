package original; /**
 * Created by nifras on 1/14/17.
 */


import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.MUX;
import org.jpos.q2.QBeanSupport;
import org.jpos.util.NameRegistrar;

/**
 * Created with IntelliJ IDEA.
 * User: ouwaifo
 * Date: 6/7/12
 * Time: 1:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChannelManager  extends QBeanSupport {

    private long MAX_TIME_OUT;
    private MUX mux;

    @Override
    protected void initService() throws ISOException {
        log.info ("initializing ChannelManager Service");
        try {
            System.out.println("*******************"+ cfg.get("mux")+"*******");
            mux = (MUX) NameRegistrar.get("mux." + cfg.get("mux"));
            //mux = (MUX) NameRegistrar.get(cfg.get("mux"));
            MAX_TIME_OUT = cfg.getLong("timeout");
            NameRegistrar.register ("manager", this);
        } catch (NameRegistrar.NotFoundException e){
            log.error("Error in initializing service :"+ e.getMessage());
        }

    }

    public ISOMsg sendMsg(ISOMsg m) throws Exception{
        return sendMsg(m, mux, MAX_TIME_OUT);
    }

    private  ISOMsg sendMsg(ISOMsg msg, MUX mux, long time)
            throws Exception
    {

        if (mux != null)
        {
            long start = System.currentTimeMillis();
            ISOMsg respMsg = mux.request(msg,time);
            long duration = System.currentTimeMillis() - start;
            log.info("Response time (ms):"+ duration);
            return respMsg;
        }
        return null;
    }

}