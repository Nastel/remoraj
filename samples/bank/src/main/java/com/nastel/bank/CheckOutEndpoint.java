package com.nastel.bank;

import com.nastel.bank.data.Transaction;

import javax.ws.rs.*;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


@Path("/payment")
public class CheckOutEndpoint {


    @GET
    @Path("/get")
    @Produces("application/json")
    public Map<String, String> get() {
        return new HashMap<String, String>() {
            {
                put("Working", "TRUE");
            }
        };
    }


    @POST
    @Path("/do")
    @Consumes("text/plain")
    @Produces("application/json")
    public boolean commitCheckOut(String body, @HeaderParam("CardNr") String cardNr, @QueryParam("amount") int amountInt) {
        int uId = DbUtils.getUserId(DbUtils.DEMO);
        float balance = DbUtils.getBalance(DbUtils.DEMO);
        float amount = amountInt /100;
        float balanceEnd = balance - amount;

        if (!DbUtils.addTransaction(uId, Transaction.XACT_BILLPAY, 0, amount, balanceEnd)) {
            return false;
        }
        return true;
    }


}
