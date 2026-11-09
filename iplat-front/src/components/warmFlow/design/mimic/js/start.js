import {BaseNodeModel} from "@/components/warmFlow/design/mimic/js/baseNodeModel";
import {BaseNodeView} from "@/components/warmFlow/design/mimic/js/baseNodeView";

class StartModel extends BaseNodeModel {}

class StartView extends BaseNodeView {}

export default {
  type: "start",
  model: StartModel,
  view: StartView,
};

