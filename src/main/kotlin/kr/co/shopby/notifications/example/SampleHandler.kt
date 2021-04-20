package kr.co.shopby.notifications.example

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks

@Configuration
class SampleHandler(private val multi: Sinks.Many<String>) : WebSocketHandler {

  @Bean("multi")
  fun multi(): Sinks.Many<String> {
    return Sinks.many()
      .multicast()
      .onBackpressureBuffer()
  }

  override fun handle(session: WebSocketSession): Mono<Void> {
    val input = session
      .receive()
      .doOnNext {
        multi.tryEmitNext(it.payloadAsText)
      }.then()

    val output = session
      .send(
        multi
          .asFlux()
          .map(session::textMessage)
      )

    return Mono.zip(input, output).then()
  }
}