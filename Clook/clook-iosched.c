/*
 * elevator clook
 * Miguel Guevara, Ekaterina Gumnova
 * COP 4610
 * Due:	04/22/2016 
 * Clook algorithm is implemented in clook_add_request
 */
#include <linux/blkdev.h>
#include <linux/elevator.h>
#include <linux/bio.h>
#include <linux/module.h>
#include <linux/slab.h>
#include <linux/init.h>

int dh = -1;

struct clook_data {
	struct list_head queue;
};

static void clook_merged_requests(struct request_queue *q, struct request *rq,
				 struct request *next)
{
	list_del_init(&next->queuelist);
}

static int clook_dispatch(struct request_queue *q, int force)
{
	struct clook_data *nd = q->elevator->elevator_data;

	if (!list_empty(&nd->queue)) {
		struct request *rq;
		rq = list_entry(nd->queue.next, struct request, queuelist);
		list_del_init(&rq->queuelist);
		elv_dispatch_sort(q, rq);
		dh = blk_rq_pos(rq);	//get the position of the head
		/*
		 * rq_data_dir(struct request *rq) extracts the direction of the 
		 * transfer from the request; zero denotes READ, nonzero denotes WRITE
		*/
		if (rq_data_dir(rq)) {
			printk("[CLOOK] dsp W %lu\n", (long)blk_rq_pos(rq));
		}
		else printk("[CLOOK] dsp R %lu\n", (long)blk_rq_pos(rq));
		return 1;
	}
	return 0;
}

static void clook_add_request(struct request_queue *q, struct request *rq)
{
	struct clook_data *nd = q->elevator->elevator_data;
	struct list_head *entry = NULL; //pointer to hold current entry while iterating
    //iterate through each entry of the request queue
	list_for_each(entry, &nd->queue) {
		struct request *req = list_entry(entry, struct request, queuelist);
		//if request is before the position of the head, insert when current position of the queue is less than position of the diskhead && current request is less than current position of the queue
        if (blk_rq_pos(rq) < dh) {
			if (blk_rq_pos(req) < dh && blk_rq_pos(rq) < blk_rq_pos(req))
				break;
		}
        //if the request is after the position of the head, insert before the next bigger current position of the queue or when the current position becomes smaller than the position of the head
		else {
			if (blk_rq_pos(req) < dh || blk_rq_pos(rq) < blk_rq_pos(req))
				break;
			}
	}
	
	if (rq_data_dir(rq)) {
			printk("[CLOOK] add W %lu\n", (long)blk_rq_pos(rq));
		}
		else printk("[CLOOK] add R %lu\n", (long)blk_rq_pos(rq));
	
	list_add_tail(&rq->queuelist, entry);
}

static int clook_queue_empty(struct request_queue *q)
{
	struct clook_data *nd = q->elevator->elevator_data;

	return list_empty(&nd->queue);
}

static struct request *
clook_former_request(struct request_queue *q, struct request *rq)
{
	struct clook_data *nd = q->elevator->elevator_data;

	if (rq->queuelist.prev == &nd->queue)
		return NULL;
	return list_entry(rq->queuelist.prev, struct request, queuelist);
}

static struct request *
clook_latter_request(struct request_queue *q, struct request *rq)
{
	struct clook_data *nd = q->elevator->elevator_data;

	if (rq->queuelist.next == &nd->queue)
		return NULL;
	return list_entry(rq->queuelist.next, struct request, queuelist);
}

static void *clook_init_queue(struct request_queue *q)
{
	struct clook_data *nd;

	nd = kmalloc_node(sizeof(*nd), GFP_KERNEL, q->node);
	if (!nd)
		return NULL;
	INIT_LIST_HEAD(&nd->queue);
	return nd;
}

static void clook_exit_queue(struct elevator_queue *e)
{
	struct clook_data *nd = e->elevator_data;

	BUG_ON(!list_empty(&nd->queue));
	kfree(nd);
}

static struct elevator_type elevator_clook = {
	.ops = {
		.elevator_merge_req_fn		= clook_merged_requests,
		.elevator_dispatch_fn		= clook_dispatch,
		.elevator_add_req_fn		= clook_add_request,
		.elevator_queue_empty_fn	= clook_queue_empty,
		.elevator_former_req_fn		= clook_former_request,
		.elevator_latter_req_fn		= clook_latter_request,
		.elevator_init_fn		= clook_init_queue,
		.elevator_exit_fn		= clook_exit_queue,
	},
	.elevator_name = "clook",
	.elevator_owner = THIS_MODULE,
};

static int __init clook_init(void)
{
	elv_register(&elevator_clook);

	return 0;
}

static void __exit clook_exit(void)
{
	elv_unregister(&elevator_clook);
}

module_init(clook_init);
module_exit(clook_exit);


MODULE_AUTHOR("Miguel Guevara, Ekaterina Gumnova");
MODULE_LICENSE("GPL");
MODULE_DESCRIPTION("Clook");
