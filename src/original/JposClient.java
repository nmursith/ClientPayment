package original; /**
 * Created by nifras on 1/14/17.
 */


import controller.Constants;
import controller.PaymentController;
import jdk.nashorn.api.scripting.JSObject;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.gui.ISOMeter;
import org.jpos.q2.Q2;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.jms.JMSException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * * Created with IntelliJ IDEA. * User: ouwaifo * Date: 6/7/12 * Time: 1:51 PM
 * * To change this template use File | Settings | File Templates.
 */
public class JposClient {
    static PaymentController paymentController;
    static void startQ2() {
        Q2 q2 = new Q2();
        q2.start();
    }

    public static void main(String[] args) throws ISOException {
        new JposClient().begin();
    }

    private  void begin() throws ISOException{
        startQ2();
        ISOUtil.sleep(5000);

        try {
            paymentController = new PaymentController("credit", "credit", this);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void execute(String message){
        try {

            JSONObject data = (JSONObject) (new JSONParser().parse(message));

            data.put("name_on_card", "Mohamed Nifras");

            data.put("card_type", "visa");
            data.put("card_number", "4032039105422911");
            data.put("expiry_month", "12");
            data.put("expiry_year", "2021");
            data.put("cvv", "123");
            data.put("orderID", "12324");
            data.put("corre-id", "123226651942");

            String name_on_card = data.get("name_on_card").toString();
            String primary_account_number= data.get("card_number").toString();
            int expiry_month = Integer.parseInt(data.get("expiry_month").toString());
            int expiry_year = Integer.parseInt(data.get("expiry_year").toString());

            String amount = data.get("amount").toString();
            String card_type = data.get("card_type").toString();
            String  cvv = data.get("cvv").toString();
            String orderID = data.get("orderID").toString();


            ISOMsg isoMsg = new ISOMsg();
            isoMsg.setMTI("0200");
            isoMsg.set(2, primary_account_number);
            isoMsg.set(3, "53"); /**  00 	Authorization (Goods and Services)
             01 	Cash (ATM)
             02 	Debit Adjustment
             20 	Refund
             30 	Available funds inquiry
             31 	Balance inquiry
             50 	Payment from account
             53 	Payment to account*/
            isoMsg.set(4, amount); //amount
            isoMsg.set(7, ISODate.getDateTime(new Date())); // transaction time
            isoMsg.set(11, String.valueOf(System.currentTimeMillis() % 1000000)); //system trace
            isoMsg.set(12, new SimpleDateFormat("HHmmss").format(new Date())); // TIME, LOCAL TRANSACTION
            isoMsg.set(13, new SimpleDateFormat("MMdd").format(new Date())); //DATE, LOCAL TRANSACTION
            isoMsg.set(14, String.valueOf(new Calendar.Builder()
                    .setDate(expiry_year, expiry_month, 01))); // expiry date
            isoMsg.set(19,"242"); //ACQUIRING INSTITUTION COUNTRY CODE
            isoMsg.set(21, "234");//FORWARDING INSTITUTION COUNTRY CODE
            isoMsg.set(22, "02"); //PAN Entry Mode, magnetics stripe
            isoMsg.set(32, "00001603307"); //ACQUIRING INSTITUTION IDENT CODE
            isoMsg.set(41, "T1603307"); //CARD ACCEPTOR TERMINAL IDENTIFICACION
            isoMsg.set(42, "D1342344");//Card acceptor identification code
            isoMsg.set(49, "144"); // sri lanka transaction currency code
            //isoMsg.set(70, "301"); //network management
            isoMsg.set(100, orderID); // transaction number
            isoMsg.set(98, name_on_card);

            /**Response code 39 is
             * ideally CVV is not mandatory for a transaction to complete.
             * It is technically possible to run a transaction successfully
             * without CVV number. When a transaction is run without CVV,
             * there are more chances of fraud and hence transaction without
             * CVV are sometimes charged high interchange fees.
             */

            /**
             * 00 	Successful approval/completion or that V.I.P. PIN verification is valid
             01 	Refer to card issuer
             02 	Refer to card issuer, special condition
             03 	Invalid merchant or service provider
             04 	Pickup card
             05 	Do not honor
             06 	Error
             07 	Pickup card, special condition (other than lost/stolen card)
             10 	Partial Approval
             51 	V.I.P. approval
             12 	Invalid transaction
             13 	Invalid amount (currency conversion field overflow)
             14 	Invalid account number (no such number)
             15 	No such issuer
             17 	Customer cancellation
             19 	Re-enter transaction
             20 	Invalid response
             21 	No action taken (unable to back out prior transaction)
             22 	Suspected Malfunction
             25 	Unable to locate record in file, or account number is missing from the inquiry
             28 	File is temporarily unavailable
             30 	Format Error
             41 	Pickup card (lost card)
             43 	Pickup card (stolen card)
             51 	Insufficient funds
             52 	No checking account
             53 	No savings account
             54 	Expired card
             55 	Incorrect PIN
             57 	Transaction not permitted to cardholder
             58 	Transaction not allowed at terminal
             59 	Suspected fraud
             61 	Activity amount limit exceeded
             62 	Restricted card (for example, in Country Exclusion table)
             63 	Security violation
             65 	Activity count limit exceeded
             68 	Response received too late
             75 	Allowable number of PIN-entry tries exceeded
             76 	Unable to locate previous message (no match on Retrieval Reference number)
             77 	Previous message located for a repeat or reversal, but repeat or reversal data are inconsistent with original message
             78 	’Blocked, first used’—The transaction is from a new cardholder, and the card has not been properly unblocked.
             80 	Visa transactions: credit issuer unavailable. Private label and check acceptance: Invalid date
             81 	PIN cryptographic error found (error found by VIC security module during PIN decryption)
             82 	Negative CAM, dCVV, iCVV, or CVV results
             83 	Unable to verify PIN
             85 	No reason to decline a request for account number verification, address verification, CVV2 verification, or a credit voucher or merchandise return
             91 	Issuer unavailable or switch inoperative (STIP not applicable or available for this transaction)
             92 	Destination cannot be found for routing
             93 	Transaction cannot be completed, violation of law
             94 	Duplicate Transmission
             95 	Reconcile error
             96 	System malfunction, System malfunction or certain field error conditions
             B1 	Surcharge amount not permitted on Visa cards (U.S. acquirers only)
             N0 	Force STIP
             N3 	Cash service not available
             N4 	Cashback request exceeds issuer limit
             N7 	Decline for CVV2 failure
             P2 	Invalid biller information
             P5 	PIN Change/Unblock request declined
             P6 	Unsafe PIN
             Q1 	Card Authentication failed
             R0 	Stop Payment Order
             R1 	Revocation of Authorization Order
             R3 	Revocation of All Authorizations Order
             XA 	Forward to issuer
             XD 	Forward to issuer
             Z3 	Unable to go online
             */





            new Thread(new Exec(isoMsg)).start();
        } catch (ISOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
    static class Exec implements Runnable {
        ChannelManager channelManager;
        ISOMsg isoMsg;


        Exec(ISOMsg isoMsg) throws ISOException {
            try {
                channelManager = ((ChannelManager) NameRegistrar.get("manager"));
                this.isoMsg = isoMsg;// createHandshakeISOMsg(); // need to use
            } catch (NameRegistrar.NotFoundException e) {
                LogEvent evt = channelManager.getLog().createError();
                evt.addMessage(e);
                evt.addMessage(NameRegistrar.getInstance());
                Logger.log(evt);
            }

            catch (Throwable t) {
                channelManager.getLog().error(t);
            }

        }

        private ISOMsg createNetworkMs() throws ISOException {
            ISOMsg networkReq = new ISOMsg();
            networkReq.setMTI("0800");
            networkReq.set(3, "123456");
            networkReq.set(7, new SimpleDateFormat("yyyyMMdd").format(new Date()));
            networkReq.set(11, "000001");
            networkReq.set(12, new SimpleDateFormat("HHmmss").format(new Date()));
            networkReq.set(13, new SimpleDateFormat("MMdd").format(new Date()));
            networkReq.set(48, "Network Ping");
            networkReq.set(70, "001");
            return networkReq;
        }

        private ISOMsg createHandshakeISOMsg() throws ISOException {
            ISOMsg m = new ISOMsg();
            m.setMTI("0200");

            m.set(7, ISODate.getDateTime(new Date()));
            m.set(11, String.valueOf(System.currentTimeMillis() % 1000000));
            m.set(32, "00001603307");
            m.set(41, "T1603307");
            m.set(70, "301");
            return m;
        }

        private void sendHandShake() throws Exception {
            try {
                //channelManager.sendMsg(createHandshakeISOMsg());
                ISOMsg reply = channelManager.sendMsg(isoMsg);
                channelManager.getLog().info("Handshake sent! ");

                if(reply.getMTI().equals(Constants.authenticatedFinancialResponse)) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("status", "1");
                    paymentController.sendMessage(jsonObject.toString());
                }
                else {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("status", "0");
                    paymentController.sendMessage(jsonObject.toString());
                }

            } catch (ISOException e1) {
                channelManager.getLog().error(
                        "ISOException :" + e1.getMessage());
            } catch (Exception e) {
                channelManager.getLog().error("Exception :" + e.getMessage());
            }
        }

        @Override
        public void run() {
            try {
                sendHandShake();
            } catch (Exception e) {
                e.printStackTrace();
            }

/*            while (true) {
                ISOUtil.sleep(10000);

            }*/
        }
    }
}