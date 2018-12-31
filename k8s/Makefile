.PHONY: all
all: namespace apply init api load

.PHONY: namespace
namespace:
	kubectl apply -f ns.yaml

.PHONY: apply
apply:
	kubectl apply -f services/c.yaml
	kubectl apply -f services/z.yaml
	kubectl apply -f services/kd.yaml
	kubectl apply -f services/ks.yaml

.PHONY: init
init:
	k8s-init/all.sh

.PHONY: api
api:
	kubectl apply -f services/td.yaml

.PHONY: load
load:
	k8s-init/load.sh