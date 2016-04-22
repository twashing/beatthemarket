
variable "access_key" {}
variable "secret_key" {}

provider "aws" {
  region = "us-west-1"
  access_key = "${var.access_key}"
  secret_key = "${var.secret_key}"
}

resource "aws_ecs_cluster" "default" {
  name = "beatthemarket"
}
  
resource "aws_ecs_service" "beatthemarket-service" {
  name            = "beatthemarket-service"
  cluster         = "${aws_ecs_cluster.default.id}"
  task_definition = "${aws_ecs_task_definition.beatthemarket-task.arn}"
  desired_count   = 1
}

resource "aws_ecs_task_definition" "beatthemarket-task" {
  family = "beatthemarket"
  container_definitions = "${file("task-definitions/beatthemarket.json")}"
  volume {
    name = "beatthemarket-home"
    host_path = "/ecs/beatthemarket-home"
  }
}

