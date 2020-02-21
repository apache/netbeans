OBJS = $(SOURCES:$(ROOT_DIR)/%.c=$(OBJ_DIR)/%.o)
OBJ_DIRS = $(SRC_DIRS:$(ROOT_DIR)/%=$(OBJ_DIR)/%)
$(OBJS) := DEP = $(@:$(OBJ_DIR)/%.o=$(ROOT_DIR)/%.c)

clean_deps:

$(OBJS): $(OBJ_DIRS) $$(DEP)
	$(COMPILE.c) -o $@ $(DEP)

.KEEP_STATE:
.KEEP_STATE_FILE: .make.state.$(CONF)

