package org.microjservice.rsocket.example

import com.google.protobuf.Int32Value
import com.google.protobuf.util.JsonFormat
import io.micronaut.context.annotation.Import
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import org.microjservice.user.AccountService
import reactor.core.publisher.Mono

/**
 * @author CoderYellow
 */
@Import(packages = ["org.microjservice.user"])
@Controller
class AccountController(private val accountService: AccountService) {

    @Get("/account/{id}")
    fun get(id: Int): Mono<String> {
        return accountService.findById(Int32Value.of(id))
            .map { JsonFormat.printer().print(it) }
    }
}