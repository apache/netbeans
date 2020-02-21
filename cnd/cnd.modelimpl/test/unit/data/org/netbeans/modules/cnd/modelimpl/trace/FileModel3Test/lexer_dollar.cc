#define	__SEGREGS_PUSH		\
	subl	$16, %esp;	\
	movw	%ds, 12(%esp);	\
	movw	%es, 8(%esp);

