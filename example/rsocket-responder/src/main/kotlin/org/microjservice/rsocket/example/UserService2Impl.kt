package org.microjservice.rsocket.example

import com.alibaba.rsocket.RSocketService
import com.github.javafaker.Faker
import jakarta.inject.Singleton
import org.microjservice.user.User
import org.microjservice.user.UserService2
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

@RSocketService(serviceInterface = UserService2::class)
@Singleton
class UserService2Impl : UserService2 {
    private val faker = Faker()

    override fun findById(id: Int): Mono<User> {
        return Mono.just(randomUser(id))
    }

    private fun randomUser(id: Int?): User {
        val user = User()
        user.id = id ?: Random().nextInt()
        user.nick = faker.name().name()
        user.phone = faker.phoneNumber().cellPhone()
        user.email = faker.internet().emailAddress()
        user.createdAt = Date()
        user.updatedAt = LocalDateTime.now()
        return user
    }
}