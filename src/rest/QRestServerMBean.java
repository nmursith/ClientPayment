package rest;

import org.jpos.q2.QBeanSupportMBean;

/**
 * Created by nifras on 2/9/17.
 */
public interface QRestServerMBean extends QBeanSupportMBean {

        void setPort(int port);
        int getPort();

}
