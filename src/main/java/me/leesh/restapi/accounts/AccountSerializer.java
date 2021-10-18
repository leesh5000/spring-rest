package me.leesh.restapi.accounts;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;


/**
 * JsonComponent로 등록해버리면, Account를 Resource로 내보낼 때마다 모든 Account는 id만 나간다.
 * 때로는 모든 Account 정보를 내보내야할 때가 있다.
 * 그래서, JsonComponent로 등록하지않고, 이벤트에서 사용할 때 (Serialize를 할때) 이것을 사용하도록 한다.
 */
public class AccountSerializer extends JsonSerializer<Account> {

    @Override
    public void serialize(Account account, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {

        gen.writeStartObject();
        gen.writeNumberField("id", account.getId());
        gen.writeEndObject();

    }
}
