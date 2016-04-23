
variable "access_key" {}
variable "secret_key" {}


provider "aws" {
  region = "us-west-1"
  access_key = "${var.access_key}"
  secret_key = "${var.secret_key}"
}

resource "aws_elb" "beathemarket-elb" {
  name = "beathemarket-elb"
  availability_zones = ["us-west-1a"]

  listener {
    instance_port = 8000
    instance_protocol = "http"
    lb_port = 80
    lb_protocol = "http"
  }

  health_check {
    healthy_threshold = 2
    unhealthy_threshold = 2
    timeout = 3
    target = "HTTP:8000/"
    interval = 30
  }

  cross_zone_load_balancing = true
  idle_timeout = 400
  connection_draining = true
  connection_draining_timeout = 400

  tags {
    Name = "beathemarket-elb"
  }
}

resource "aws_iam_role" "beathemarket-role" {
    name = "beathemarket-role"
    assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
}

resource "aws_ecs_cluster" "default" {
  name = "beatthemarket"
}


resource "aws_ecs_service" "beatthemarket_service" {
  name            = "beatthemarket-service"
  cluster         = "${aws_ecs_cluster.default.id}"
  task_definition = "${aws_ecs_task_definition.beatthemarket-task.arn}"
  desired_count   = 1
  iam_role = "${aws_iam_role.beathemarket-role.arn}"
  load_balancer {
    elb_name = "${aws_elb.beathemarket-elb.name}"
    container_name = "beatthemarket_service"
    container_port = 8080
  }
}

resource "aws_ecs_task_definition" "beatthemarket-task" {
  family = "beatthemarket"
  container_definitions = "${file("task-definitions/beatthemarket.json")}"
  volume {
    name = "beatthemarket-home"
    host_path = "/ecs/beatthemarket-home"
  }
}

