package amqp

import zio._
import zio.blocking._
import zio.clock._
import zio.console._
import java.net.URI
// import nl.vroste.zio.amqp._
import amqp._

object HelloWorld extends zio.App {
  type Env = Blocking with Console with Clock

  override def run(args: List[String]): ZIO[Blocking with Console with Clock, Nothing, ExitCode] =
    (for {
      _ <- putStrLn("Starting listeners...")
      _ <- listener0.forkDaemon
      _ <- listener1.forkDaemon
      forever <- ZIO.never
    } yield forever).exitCode

  val listener1: ZIO[Env, Throwable, Unit] = for {
    _ <- putStrLn("Starting listener 1")
    _ <- Amqp.connect(URI.create("amqp://guest:guest@localhost:5672")).use { conn => 
          Amqp.createChannel(conn).use { cin =>
            putStrLn("Starting to consume 1") *>
            cin.consume(queue = "topic", consumerTag = "example")
            .mapM(rawMsg => putStrLn("foo bar baz"))
            .runDrain
          }
    }.forkDaemon
  } yield ()

  val listener0: ZIO[Env, Throwable, Unit] = for {
    _ <- putStrLn("Starting listener 0")
    _ <- Amqp.connect(URI.create("amqp://guest:guest@localhost:5672")).use { conn => 
        Amqp.createChannel(conn).use { cin =>
          putStrLn("Starting to consume 0") *>
          cin.consume(queue = "topic", consumerTag = "example")
          .mapM(rawMsg => putStrLn("foo bar baz"))
          .runDrain
        }
      }
  } yield ()
}
