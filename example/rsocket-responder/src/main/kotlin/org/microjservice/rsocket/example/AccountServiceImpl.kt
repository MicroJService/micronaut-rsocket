package org.microjservice.rsocket.example

import com.alibaba.rsocket.RSocketService
import org.microjservice.user.AccountService
import com.github.javafaker.Faker
import com.google.protobuf.Int32Value
import jakarta.inject.Singleton
import org.microjservice.user.Account
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import java.util.*

/**
 * account service implementation
 *
 * @author CoderYEllow
 */
@RSocketService(serviceInterface = AccountService::class)
@Singleton
class AccountServiceImpl : AccountService {
    private val faker = Faker()
    override fun findById(id: Int32Value): Mono<Account> {
        return Mono.just(
            Account.newBuilder().setId(id.value)
                .setEmail(faker.internet().emailAddress())
                .setPhone(faker.phoneNumber().cellPhone())
                .setNick(faker.name().name())
                .build()
        )
    }

    override fun findByStatus(status: Int32Value): Flux<Account> {
        return Flux.just(
            Account.newBuilder().setId(Random().nextInt())
                .setEmail(faker.internet().emailAddress())
                .setPhone(faker.phoneNumber().cellPhone())
                .setNick(faker.name().name())
                .setStatus(status.value)
                .build(),
            Account.newBuilder().setId(Random().nextInt())
                .setEmail(faker.internet().emailAddress())
                .setPhone(faker.phoneNumber().cellPhone())
                .setNick(faker.name().name())
                .setStatus(status.value)
                .build()
        )
    }

    override fun findByIdStream(idStream: Flux<Int32Value>): Flux<Account> {
        return idStream.map { id: Int32Value ->
            Account.newBuilder()
                .setId(id.value)
                .setEmail(faker.internet().emailAddress())
                .setPhone(faker.phoneNumber().cellPhone())
                .setNick(faker.name().name())
                .setStatus(1)
                .build()
        }
    }
}