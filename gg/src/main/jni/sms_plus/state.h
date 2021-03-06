
#ifndef _STATE_H_
#define _STATE_H_



#define STATE_VERSION   0x0102      /* Version 1.1 (BCD) */
#define STATE_HEADER    "SST\0"     /* State file header */

/* Function prototypes */
void system_save_state(void *fd);
int system_save_state_mem(char *buf);
void system_load_state(void *fd);
void system_load_state_mem(char *buf);


#endif /* _STATE_H_ */
